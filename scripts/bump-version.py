#!/usr/bin/env python3

"""
Version Bumping Script for Spectra Logger
This script automates the process of bumping versions across all packages.

Usage:
    python3 scripts/bump-version.py --from 0.0.1 --to 0.0.2
    python3 scripts/bump-version.py --major      # 0.0.1 -> 1.0.0
    python3 scripts/bump-version.py --minor      # 0.0.1 -> 0.1.0
    python3 scripts/bump-version.py --patch      # 0.0.1 -> 0.0.2
    python3 scripts/bump-version.py --snapshot   # 0.0.1 -> 0.0.2-SNAPSHOT
"""

import re
import sys
import os
import subprocess
from pathlib import Path
from typing import Optional, Tuple


class VersionManager:
    def __init__(self, project_root: Path):
        self.project_root = project_root
        self.gradle_properties = project_root / "gradle.properties"
        self.changelog = project_root / "CHANGELOG.md"

    def get_current_version(self) -> str:
        """Read current version from gradle.properties"""
        with open(self.gradle_properties, "r") as f:
            for line in f:
                if line.startswith("VERSION_NAME="):
                    return line.split("=", 1)[1].strip()
        raise RuntimeError("VERSION_NAME not found in gradle.properties")

    def parse_version(self, version_str: str) -> Tuple[int, int, int, Optional[str]]:
        """Parse version string into components

        Returns: (major, minor, patch, suffix) or (major, minor, patch, None)
        Examples:
            "0.0.1" -> (0, 0, 1, None)
            "0.0.2-SNAPSHOT" -> (0, 0, 2, "SNAPSHOT")
            "0.0.2-beta1" -> (0, 0, 2, "beta1")
        """
        # Remove suffix if present
        match = re.match(r"^(\d+)\.(\d+)\.(\d+)(?:-(.+))?$", version_str)
        if not match:
            raise ValueError(f"Invalid version format: {version_str}")

        major, minor, patch, suffix = match.groups()
        return int(major), int(minor), int(patch), suffix

    def format_version(self, major: int, minor: int, patch: int, suffix: Optional[str] = None) -> str:
        """Format version back to string"""
        version = f"{major}.{minor}.{patch}"
        if suffix:
            version += f"-{suffix}"
        return version

    def bump_version(
        self,
        from_version: Optional[str] = None,
        to_version: Optional[str] = None,
        major: bool = False,
        minor: bool = False,
        patch: bool = False,
        snapshot: bool = False,
    ) -> Tuple[str, str]:
        """Bump version and return (old_version, new_version)"""

        current = self.get_current_version()
        print(f"Current version: {current}")

        if from_version and from_version != current:
            raise ValueError(f"From version {from_version} doesn't match current {current}")

        # Determine new version
        if to_version:
            new = to_version
        elif major or minor or patch or snapshot:
            maj, min_, patch_, suff = self.parse_version(current)

            if major:
                new = self.format_version(maj + 1, 0, 0)
            elif minor:
                new = self.format_version(maj, min_ + 1, 0)
            elif patch:
                new = self.format_version(maj, min_, patch_ + 1)
            elif snapshot:
                # Increment patch and add -SNAPSHOT
                new = self.format_version(maj, min_, patch_ + 1, "SNAPSHOT")
        else:
            raise ValueError("Must specify --to, --major, --minor, --patch, or --snapshot")

        print(f"New version: {new}")
        return current, new

    def update_gradle_properties(self, old_version: str, new_version: str) -> None:
        """Update VERSION_NAME in gradle.properties"""
        with open(self.gradle_properties, "r") as f:
            content = f.read()

        content = content.replace(f"VERSION_NAME={old_version}", f"VERSION_NAME={new_version}")

        with open(self.gradle_properties, "w") as f:
            f.write(content)

        print(f"✓ Updated gradle.properties")

    def validate_files_exist(self) -> None:
        """Validate all key files exist"""
        required_files = [
            self.gradle_properties,
            self.project_root / "shared" / "build.gradle.kts",
            self.project_root / "SpectraLogger" / "Package.swift",
            self.project_root / "SpectraLoggerUI" / "Package.swift",
        ]

        for file_path in required_files:
            if not file_path.exists():
                raise FileNotFoundError(f"Required file not found: {file_path}")

        print("✓ All required files exist")

    def run_validation_script(self) -> bool:
        """Run the version sync validation script"""
        script_path = self.project_root / "scripts" / "sync-versions.sh"
        if not script_path.exists():
            print("⚠ sync-versions.sh not found, skipping validation")
            return False

        result = subprocess.run(
            ["bash", str(script_path)],
            cwd=str(self.project_root),
            capture_output=True,
            text=True,
        )

        if result.returncode == 0:
            print("✓ Version validation passed")
            return True
        else:
            print(f"✗ Version validation failed:\n{result.stderr}")
            return False

    def print_summary(self, old_version: str, new_version: str) -> None:
        """Print summary of changes"""
        print("\n" + "=" * 60)
        print("VERSION BUMP SUMMARY")
        print("=" * 60)
        print(f"Old version: {old_version}")
        print(f"New version: {new_version}")
        print("\nNext steps:")
        print("  1. Verify changes: git diff")
        print("  2. Commit: git add . && git commit -m 'release: bump to {}'"
              .format(new_version))
        print("  3. Tag: git tag -a v{} -m 'Release {}'"
              .format(new_version, new_version))
        print("  4. Push: git push origin v{} && git push"
              .format(new_version))
        print("=" * 60 + "\n")


def main():
    import argparse

    parser = argparse.ArgumentParser(
        description="Bump Spectra Logger version across all packages",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )

    parser.add_argument("--from", dest="from_version", help="From version (validated against current)")
    parser.add_argument("--to", dest="to_version", help="Target version (e.g., 0.0.2)")
    parser.add_argument("--major", action="store_true", help="Bump major version")
    parser.add_argument("--minor", action="store_true", help="Bump minor version")
    parser.add_argument("--patch", action="store_true", help="Bump patch version")
    parser.add_argument("--snapshot", action="store_true", help="Bump patch and add -SNAPSHOT suffix")
    parser.add_argument("--dry-run", action="store_true", help="Show what would change without applying")

    args = parser.parse_args()

    try:
        project_root = Path(__file__).parent.parent
        manager = VersionManager(project_root)

        # Validate files exist
        manager.validate_files_exist()

        # Parse arguments and bump version
        old_version, new_version = manager.bump_version(
            from_version=args.from_version,
            to_version=args.to_version,
            major=args.major,
            minor=args.minor,
            patch=args.patch,
            snapshot=args.snapshot,
        )

        if args.dry_run:
            print("\n[DRY RUN] Changes would be:")
            print(f"  gradle.properties: VERSION_NAME={old_version} -> VERSION_NAME={new_version}")
            manager.print_summary(old_version, new_version)
            return 0

        # Apply changes
        manager.update_gradle_properties(old_version, new_version)

        # Validate
        if not manager.run_validation_script():
            print("⚠ Warning: Validation script had issues")

        # Print summary
        manager.print_summary(old_version, new_version)

        return 0

    except Exception as e:
        print(f"✗ Error: {e}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())

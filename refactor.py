import os
import shutil
import re

# Base paths
CORE_SRC = 'spectra-core/src'
UI_SRC = 'spectra-ui/src'

# Rules: (class_name, old_package, new_package, new_dir_suffix)
# We map simple class names to their new package paths.
# If a file matches the class, it moves to the new path.
# All instances of the old package + class are replaced with the new package + class.
# All package statements in the file are updated.

file_mappings = {
    # Core - Logs Feature
    'LogEntry': ('com.spectra.logger.domain.model', 'com.spectra.logger.feature.logs.model'),
    'LogLevel': ('com.spectra.logger.domain.model', 'com.spectra.logger.feature.logs.model'),
    'LogFilter': ('com.spectra.logger.domain.model', 'com.spectra.logger.feature.logs.model'),
    'LogStorage': ('com.spectra.logger.domain.storage', 'com.spectra.logger.feature.logs.storage'),
    'InMemoryLogStorage': ('com.spectra.logger.domain.storage', 'com.spectra.logger.feature.logs.storage'),
    'FileLogStorage': ('com.spectra.logger.domain.storage', 'com.spectra.logger.feature.logs.storage'),
    'FilterEngineRepository': ('com.spectra.logger.domain.statistics', 'com.spectra.logger.feature.logs.statistics'),
    'DashboardStatistics': ('com.spectra.logger.domain.statistics', 'com.spectra.logger.feature.logs.statistics'),
    'LogExporter': ('com.spectra.logger.export', 'com.spectra.logger.feature.logs.export'),
    'FileExporter': ('com.spectra.logger.export', 'com.spectra.logger.feature.logs.export'),
    
    # Core - Network Feature
    'NetworkLogEntry': ('com.spectra.logger.domain.model', 'com.spectra.logger.feature.network.model'),
    'NetworkLogFilter': ('com.spectra.logger.domain.model', 'com.spectra.logger.feature.network.model'),
    'NetworkLogStorage': ('com.spectra.logger.domain.storage', 'com.spectra.logger.feature.network.storage'),
    'InMemoryNetworkLogStorage': ('com.spectra.logger.domain.storage', 'com.spectra.logger.feature.network.storage'),
    'SpectraNetworkLogger': ('com.spectra.logger.network', 'com.spectra.logger.feature.network.interceptor'),
    'SpectraURLSessionLogger': ('com.spectra.logger.network', 'com.spectra.logger.feature.network.interceptor'),
    'SpectraNetworkInterceptor': ('com.spectra.logger.network', 'com.spectra.logger.feature.network.interceptor'),

    # Core - Settings
    'LoggerConfiguration': ('com.spectra.logger.config', 'com.spectra.logger.feature.settings.config'),

    # Core - Core Primitives
    'AppContext': ('com.spectra.logger.domain.model', 'com.spectra.logger.core.model'),
    'SourceType': ('com.spectra.logger.domain.model', 'com.spectra.logger.core.model'),
    'Logger': ('com.spectra.logger.domain', 'com.spectra.logger.core'),

    # UI - Logs
    'LogsScreen': ('com.spectra.logger.ui.compose', 'com.spectra.logger.feature.logs.ui'),
    'LogsViewModel': ('com.spectra.logger.ui.compose', 'com.spectra.logger.feature.logs.ui'),
    'LogDetailPane': ('com.spectra.logger.ui.compose', 'com.spectra.logger.feature.logs.ui'),
    'LogsFilterSheet': ('com.spectra.logger.ui.compose', 'com.spectra.logger.feature.logs.ui'),
    'DashboardScreen': ('com.spectra.logger.ui.compose', 'com.spectra.logger.feature.logs.ui'),
    'StatisticsViewModel': ('com.spectra.logger.ui.compose', 'com.spectra.logger.feature.logs.ui'),

    # UI - Network
    'NetworkLogsScreen': ('com.spectra.logger.ui.compose', 'com.spectra.logger.feature.network.ui'),
    'NetworkLogsViewModel': ('com.spectra.logger.ui.compose', 'com.spectra.logger.feature.network.ui'),
    'NetworkDetailPane': ('com.spectra.logger.ui.compose', 'com.spectra.logger.feature.network.ui'),
    'NetworkFilterSheet': ('com.spectra.logger.ui.compose', 'com.spectra.logger.feature.network.ui'),

    # UI - Settings
    'SettingsScreen': ('com.spectra.logger.ui.compose', 'com.spectra.logger.feature.settings.ui'),
    'SettingsViewModel': ('com.spectra.logger.ui.compose', 'com.spectra.logger.feature.settings.ui'),
}

# Add tests mapping
for key, (old_pkg, new_pkg) in list(file_mappings.items()):
    file_mappings[key + 'Test'] = (old_pkg, new_pkg)

# General package migrations (for files not explicitly named, e.g. components, utils)
dir_mappings = {
    'com.spectra.logger.ui.compose.components': 'com.spectra.logger.core.ui.components',
    'com.spectra.logger.ui.compose.model': 'com.spectra.logger.core.ui.model',
    'com.spectra.logger.ui.compose.navigation': 'com.spectra.logger.core.ui.navigation',
    'com.spectra.logger.ui.theme': 'com.spectra.logger.core.ui.theme',
    'com.spectra.logger.ui.util': 'com.spectra.logger.core.ui.util',
    'com.spectra.logger.ui': 'com.spectra.logger.core.ui',
    'com.spectra.logger.utils': 'com.spectra.logger.core.utils',
    'com.spectra.logger.ui.compose': 'com.spectra.logger.core.ui.compose', # base fallback
}

def get_new_package(old_pkg, filename):
    basename = filename.split('.')[0]
    # Handle platform specific suffixes like SourceDetector.android.kt -> SourceDetector
    if '.' in basename:
        basename = basename.split('.')[0]
        
    if basename in file_mappings:
        return file_mappings[basename][1]
    
    # Check dir mappings
    for old_prefix, new_prefix in dir_mappings.items():
        if old_pkg.startswith(old_prefix):
            return new_prefix + old_pkg[len(old_prefix):]
    
    return old_pkg

def get_kt_files():
    files = []
    for root_dir in [CORE_SRC, UI_SRC]:
        for dirpath, _, filenames in os.walk(root_dir):
            for f in filenames:
                if f.endswith('.kt'):
                    files.append(os.path.join(dirpath, f))
    return files

all_files = get_kt_files()

# 1. Parse current packages for each file
file_info = [] # dict of path, old_pkg, new_pkg, new_path
for filepath in all_files:
    with open(filepath, 'r') as f:
        content = f.read()
    
    pkg_match = re.search(r'^package\s+([a-zA-Z0-9_.]+)', content, re.MULTILINE)
    if not pkg_match:
        continue
    old_pkg = pkg_match.group(1)
    
    filename = os.path.basename(filepath)
    new_pkg = get_new_package(old_pkg, filename)
    
    if old_pkg != new_pkg:
        # Calculate new path
        old_path_suffix = old_pkg.replace('.', '/')
        new_path_suffix = new_pkg.replace('.', '/')
        new_filepath = filepath.replace(old_path_suffix, new_path_suffix)
        file_info.append({
            'filepath': filepath,
            'new_filepath': new_filepath,
            'old_pkg': old_pkg,
            'new_pkg': new_pkg,
            'filename': filename
        })

# 2. Build import replacements map
import_replacements = {}
for info in file_info:
    # Handle files named like `SourceDetector.android.kt` correctly for imports
    basename = info['filename'].split('.')[0] 
    old_import = info['old_pkg'] + '.' + basename
    new_import = info['new_pkg'] + '.' + basename
    import_replacements[old_import] = new_import

# 3. Apply moves and content updates
for info in file_info:
    # Ensure dir exists
    os.makedirs(os.path.dirname(info['new_filepath']), exist_ok=True)
    
    # Move file using git if possible, or shutil
    if os.system(f"git mv {info['filepath']} {info['new_filepath']}") != 0:
        shutil.move(info['filepath'], info['new_filepath'])

# Update contents of all files at their new locations
updated_files = get_kt_files()
for filepath in updated_files:
    with open(filepath, 'r') as f:
        content = f.read()
    
    new_content = content
    # Update package declaration
    # We must do this carefully, replacing only the package line
    for info in file_info:
        if info['new_filepath'] == filepath:
            new_content = re.sub(r'^package\s+' + re.escape(info['old_pkg']), r'package ' + info['new_pkg'], new_content, flags=re.MULTILINE)
            break
            
    # Update imports
    for old_import, new_import in import_replacements.items():
        new_content = re.sub(r'^import\s+' + re.escape(old_import), r'import ' + new_import, new_content, flags=re.MULTILINE)
    
    # Also update directory mappings in imports like `import com.spectra.logger.ui.theme.*`
    for old_dir, new_dir in dir_mappings.items():
        new_content = re.sub(r'^import\s+' + re.escape(old_dir) + r'\.', r'import ' + new_dir + '.', new_content, flags=re.MULTILINE)

    if new_content != content:
        with open(filepath, 'w') as f:
            f.write(new_content)

print("Refactoring complete.")

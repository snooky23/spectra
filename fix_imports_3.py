import os
import re

CORE_SRC = 'spectra-core/src'
UI_SRC = 'spectra-ui/src'

def get_kt_files():
    files = []
    for root_dir in [CORE_SRC, UI_SRC]:
        for dirpath, _, filenames in os.walk(root_dir):
            for f in filenames:
                if f.endswith('.kt'):
                    files.append(os.path.join(dirpath, f))
    return files

all_files = get_kt_files()

for filepath in all_files:
    with open(filepath, 'r') as f:
        content = f.read()
    
    new_content = content
    
    # 1. Fix explicit utils references
    new_content = re.sub(r'com\.spectra\.logger\.utils\.', r'com.spectra.logger.core.utils.', new_content)
    
    # 2. Fix conflicting/duplicate imports
    # Read lines, keep track of imports to remove duplicates
    lines = new_content.split('\n')
    out_lines = []
    seen_imports = set()
    
    for line in lines:
        if line.startswith('import '):
            if line in seen_imports:
                continue
            seen_imports.add(line)
        out_lines.append(line)
    
    new_content = '\n'.join(out_lines)

    if new_content != content:
        with open(filepath, 'w') as f:
            f.write(new_content)

print("Fixes applied.")

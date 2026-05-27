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

missing_imports = [
    'import com.spectra.logger.core.utils.*',
    'import com.spectra.logger.core.model.SourceType',
    'import com.spectra.logger.feature.network.model.NetworkLogFilter',
    'import com.spectra.logger.core.model.*',
]

for filepath in all_files:
    with open(filepath, 'r') as f:
        content = f.read()
    
    # We will inject these imports right after the package declaration
    package_match = re.search(r'^package\s+[a-zA-Z0-9_.]+', content, re.MULTILINE)
    if package_match:
        idx = package_match.end()
        new_content = content[:idx] + '\n\n' + '\n'.join(missing_imports) + content[idx:]
        
        if new_content != content:
            with open(filepath, 'w') as f:
                f.write(new_content)

print("Injected imports.")

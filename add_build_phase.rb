require 'xcodeproj'

project_path = 'examples/ios-native/SpectraExample.xcodeproj'
project = Xcodeproj::Project.open(project_path)
target = project.targets.find { |t| t.name == 'SpectraExample' }

phase_name = 'Build KMP Spectra Frameworks'
existing_phase = target.build_phases.find { |p| p.respond_to?(:name) && p.name == phase_name }

unless existing_phase
  phase = project.new(Xcodeproj::Project::Object::PBXShellScriptBuildPhase)
  phase.name = phase_name
  phase.shell_script = "cd \"$SRCROOT/../../..\"\nexport SPECTRA_LOCAL_DEV=1\n./scripts/build/build-xcframework.sh ${CONFIGURATION}"
  
  target.build_phases.insert(0, phase)
  project.save
  puts "✅ Added Run Script Phase successfully!"
else
  puts "✅ Phase already exists."
end

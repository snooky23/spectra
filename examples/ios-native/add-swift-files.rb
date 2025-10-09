#!/usr/bin/env ruby

require 'xcodeproj'

project_path = 'SpectraExample.xcodeproj'
project = Xcodeproj::Project.open(project_path)

# Get the main target
target = project.targets.first

# Get the main group
main_group = project.main_group['SpectraExample']

# Create ViewModels group if it doesn't exist
viewmodels_group = main_group['ViewModels'] || main_group.new_group('ViewModels')

# Create Views group if it doesn't exist
views_group = main_group['Views'] || main_group.new_group('Views')

# Add ViewModel files
viewmodel_files = [
  'SpectraExample/ViewModels/LogsViewModel.swift',
  'SpectraExample/ViewModels/NetworkLogsViewModel.swift',
  'SpectraExample/ViewModels/SettingsViewModel.swift'
]

viewmodel_files.each do |file_path|
  file_name = File.basename(file_path)
  # Check if file already exists in project
  existing_file = viewmodels_group.files.find { |f| f.path == file_name }
  unless existing_file
    file_ref = viewmodels_group.new_file(file_path)
    target.source_build_phase.add_file_reference(file_ref)
    puts "Added #{file_name} to ViewModels group"
  else
    puts "#{file_name} already exists in project"
  end
end

# Add View files
view_files = [
  'SpectraExample/Views/LogsView.swift',
  'SpectraExample/Views/NetworkLogsView.swift',
  'SpectraExample/Views/SettingsView.swift'
]

view_files.each do |file_path|
  file_name = File.basename(file_path)
  # Check if file already exists in project
  existing_file = views_group.files.find { |f| f.path == file_name }
  unless existing_file
    file_ref = views_group.new_file(file_path)
    target.source_build_phase.add_file_reference(file_ref)
    puts "Added #{file_name} to Views group"
  else
    puts "#{file_name} already exists in project"
  end
end

project.save

puts "Project updated successfully!"

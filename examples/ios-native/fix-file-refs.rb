#!/usr/bin/env ruby

require 'xcodeproj'

project_path = 'SpectraExample.xcodeproj'
project = Xcodeproj::Project.open(project_path)

# Get the main group
main_group = project.main_group['SpectraExample']

# Remove ViewModels and Views groups if they exist
viewmodels_group = main_group['ViewModels']
views_group = main_group['Views']

viewmodels_group&.clear
views_group&.clear

# Add files with correct paths relative to the group
if viewmodels_group
  viewmodel_files = [
    '../ViewModels/LogsViewModel.swift',
    '../ViewModels/NetworkLogsViewModel.swift',
    '../ViewModels/SettingsViewModel.swift'
  ]

  viewmodel_files.each do |file_path|
    file_name = File.basename(file_path)
    file_ref = viewmodels_group.new_file(file_path)
    puts "Added #{file_name} with path: #{file_path}"
  end
end

if views_group
  view_files = [
    '../Views/LogsView.swift',
    '../Views/NetworkLogsView.swift',
    '../Views/SettingsView.swift'
  ]

  view_files.each do |file_path|
    file_name = File.basename(file_path)
    file_ref = views_group.new_file(file_path)
    puts "Added #{file_name} with path: #{file_path}"
  end
end

project.save

puts "Project file references fixed!"

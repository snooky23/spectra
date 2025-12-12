import SwiftUI
import SpectraLogger

/// Filter configuration for the Logs screen
struct LogsFilter: Equatable {
    var selectedTags: Set<String> = []
    var customTags: Set<String> = []  // Tags added by user that may not exist in logs yet
    var fromTimestamp: Date? = nil
    var toTimestamp: Date? = nil
    var metadataKey: String = ""
    var metadataValue: String = ""
    var hasErrorOnly: Bool = false
    
    /// All tags to filter by (existing + custom)
    var allSelectedTags: Set<String> {
        selectedTags.union(customTags)
    }
    
    /// Check if any filters are active
    var hasActiveFilters: Bool {
        !selectedTags.isEmpty ||
        !customTags.isEmpty ||
        fromTimestamp != nil ||
        toTimestamp != nil ||
        (!metadataKey.isEmpty && !metadataValue.isEmpty) ||
        hasErrorOnly
    }
    
    /// Count of active filter types
    var activeFilterCount: Int {
        var count = 0
        if !allSelectedTags.isEmpty { count += 1 }
        if fromTimestamp != nil || toTimestamp != nil { count += 1 }
        if !metadataKey.isEmpty && !metadataValue.isEmpty { count += 1 }
        if hasErrorOnly { count += 1 }
        return count
    }
    
    mutating func reset() {
        selectedTags = []
        customTags = []
        fromTimestamp = nil
        toTimestamp = nil
        metadataKey = ""
        metadataValue = ""
        hasErrorOnly = false
    }
}

/// Full-screen modal for advanced log filtering
struct LogsFilterView: View {
    @Binding var filter: LogsFilter
    let availableTags: [String]
    let onApply: () -> Void
    
    @Environment(\.dismiss) private var dismiss
    @State private var customTagInput = ""
    @State private var showFromDatePicker = false
    @State private var showToDatePicker = false
    
    // Quick preset options
    private let timePresets: [(String, TimeInterval)] = [
        ("Last hour", -3600),
        ("Last 24h", -86400),
        ("Last 7 days", -604800)
    ]
    
    var body: some View {
        NavigationView {
            Form {
                // MARK: - Tags Section
                Section {
                    // Custom tag input
                    HStack {
                        TextField("Add custom tag...", text: $customTagInput)
                            .textFieldStyle(.plain)
                        
                        if !customTagInput.isEmpty {
                            Button(action: addCustomTag) {
                                Image(systemName: "plus.circle.fill")
                                    .foregroundColor(.blue)
                            }
                        }
                    }
                    
                    // Custom tags (user-added)
                    ForEach(Array(filter.customTags).sorted(), id: \.self) { tag in
                        HStack {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundColor(.blue)
                            Text(tag)
                            Spacer()
                            Text("(custom)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Button(action: { filter.customTags.remove(tag) }) {
                                Image(systemName: "xmark.circle")
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                    
                    // Existing tags from logs
                    ForEach(availableTags, id: \.self) { tag in
                        Button(action: { toggleTag(tag) }) {
                            HStack {
                                Image(systemName: filter.selectedTags.contains(tag) ? "checkmark.circle.fill" : "circle")
                                    .foregroundColor(filter.selectedTags.contains(tag) ? .blue : .secondary)
                                Text(tag)
                                    .foregroundColor(.primary)
                                Spacer()
                            }
                        }
                    }
                    
                    if availableTags.isEmpty && filter.customTags.isEmpty {
                        Text("No tags available. Add a custom tag to filter for future logs.")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                } header: {
                    Text("Tags")
                } footer: {
                    Text("Select existing tags or add custom tags to filter for logs that may appear later")
                }
                
                // MARK: - Time Range Section
                Section {
                    // Quick presets
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(timePresets, id: \.0) { preset in
                                Button(preset.0) {
                                    filter.fromTimestamp = Date(timeIntervalSinceNow: preset.1)
                                    filter.toTimestamp = nil
                                }
                                .buttonStyle(.bordered)
                                .controlSize(.small)
                            }
                            
                            Button("Clear") {
                                filter.fromTimestamp = nil
                                filter.toTimestamp = nil
                            }
                            .buttonStyle(.bordered)
                            .controlSize(.small)
                            .tint(.red)
                        }
                    }
                    .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                    
                    // From date
                    HStack {
                        Text("From")
                        Spacer()
                        if let from = filter.fromTimestamp {
                            Text(DateFormattingUtilities.formatFullTimestamp(from.toKotlinInstant()))
                                .foregroundColor(.secondary)
                        } else {
                            Text("Not set")
                                .foregroundColor(.secondary)
                        }
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        showFromDatePicker.toggle()
                    }
                    
                    if showFromDatePicker {
                        DatePicker(
                            "From Date",
                            selection: Binding(
                                get: { filter.fromTimestamp ?? Date() },
                                set: { filter.fromTimestamp = $0 }
                            ),
                            displayedComponents: [.date, .hourAndMinute]
                        )
                        .datePickerStyle(.graphical)
                    }
                    
                    // To date
                    HStack {
                        Text("To")
                        Spacer()
                        if let to = filter.toTimestamp {
                            Text(DateFormattingUtilities.formatFullTimestamp(to.toKotlinInstant()))
                                .foregroundColor(.secondary)
                        } else {
                            Text("Not set")
                                .foregroundColor(.secondary)
                        }
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        showToDatePicker.toggle()
                    }
                    
                    if showToDatePicker {
                        DatePicker(
                            "To Date",
                            selection: Binding(
                                get: { filter.toTimestamp ?? Date() },
                                set: { filter.toTimestamp = $0 }
                            ),
                            displayedComponents: [.date, .hourAndMinute]
                        )
                        .datePickerStyle(.graphical)
                    }
                } header: {
                    Text("Time Range")
                }
                
                // MARK: - Metadata Section
                Section {
                    TextField("Key (e.g., user_id)", text: $filter.metadataKey)
                    TextField("Value (e.g., 12345)", text: $filter.metadataValue)
                } header: {
                    Text("Metadata")
                } footer: {
                    Text("Filter logs containing specific metadata key-value pairs")
                }
                
                // MARK: - Error Toggle
                Section {
                    Toggle("Show only logs with errors", isOn: $filter.hasErrorOnly)
                } header: {
                    Text("Errors")
                } footer: {
                    Text("Filter to show only logs that have an attached exception or stack trace")
                }
            }
            .navigationTitle("Filters")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Reset") {
                        filter.reset()
                    }
                    .foregroundColor(.red)
                }
                
                ToolbarItem(placement: .bottomBar) {
                    Button(action: {
                        onApply()
                        dismiss()
                    }) {
                        Text("Apply Filters")
                            .fontWeight(.semibold)
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.large)
                }
            }
        }
    }
    
    private func addCustomTag() {
        let tag = customTagInput.trimmingCharacters(in: .whitespacesAndNewlines)
        if !tag.isEmpty && !filter.customTags.contains(tag) && !availableTags.contains(tag) {
            filter.customTags.insert(tag)
        }
        customTagInput = ""
    }
    
    private func toggleTag(_ tag: String) {
        if filter.selectedTags.contains(tag) {
            filter.selectedTags.remove(tag)
        } else {
            filter.selectedTags.insert(tag)
        }
    }
}

/// Badge showing an active filter that can be removed
struct ActiveFilterBadge: View {
    let label: String
    let onRemove: () -> Void
    
    var body: some View {
        HStack(spacing: 4) {
            Text(label)
                .font(.caption)
            Button(action: onRemove) {
                Image(systemName: "xmark.circle.fill")
                    .font(.caption)
            }
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 6)
        .background(Color.blue.opacity(0.15))
        .foregroundColor(.blue)
        .cornerRadius(16)
    }
}

// MARK: - Date Extension for Kotlin Interop

extension Date {
    func toKotlinInstant() -> Kotlinx_datetimeInstant {
        let epochSeconds = Int64(self.timeIntervalSince1970)
        return Kotlinx_datetimeInstant.companion.fromEpochSeconds(epochSeconds: epochSeconds, nanosecondAdjustment: 0)
    }
}

// MARK: - Previews

#Preview("LogsFilterView") {
    LogsFilterView(
        filter: .constant(LogsFilter()),
        availableTags: ["Auth", "Network", "Database", "UI"],
        onApply: {}
    )
}

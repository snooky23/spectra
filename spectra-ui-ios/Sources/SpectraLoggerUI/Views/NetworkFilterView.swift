import SwiftUI
import SpectraLogger

/// Filter configuration for the Network screen
struct NetworkFilter: Equatable {
    var selectedMethods: Set<String> = []
    var selectedStatusRanges: Set<String> = []
    var hostPattern: String = ""
    var fromTimestamp: Date? = nil
    var toTimestamp: Date? = nil
    var responseTimeThreshold: ResponseTimeThreshold? = nil
    var showOnlyFailed: Bool = false
    
    enum ResponseTimeThreshold: String, CaseIterable {
        case over100ms = "> 100ms"
        case over500ms = "> 500ms"
        case over1s = "> 1s"
        
        var milliseconds: Int64 {
            switch self {
            case .over100ms: return 100
            case .over500ms: return 500
            case .over1s: return 1000
            }
        }
    }
    
    /// Check if any filters are active
    var hasActiveFilters: Bool {
        !selectedMethods.isEmpty ||
        !selectedStatusRanges.isEmpty ||
        !hostPattern.isEmpty ||
        fromTimestamp != nil ||
        toTimestamp != nil ||
        responseTimeThreshold != nil ||
        showOnlyFailed
    }
    
    /// Count of active filter types
    var activeFilterCount: Int {
        var count = 0
        if !selectedMethods.isEmpty { count += 1 }
        if !selectedStatusRanges.isEmpty { count += 1 }
        if !hostPattern.isEmpty { count += 1 }
        if fromTimestamp != nil || toTimestamp != nil { count += 1 }
        if responseTimeThreshold != nil { count += 1 }
        if showOnlyFailed { count += 1 }
        return count
    }
    
    mutating func reset() {
        selectedMethods = []
        selectedStatusRanges = []
        hostPattern = ""
        fromTimestamp = nil
        toTimestamp = nil
        responseTimeThreshold = nil
        showOnlyFailed = false
    }
}

/// Full-screen modal for advanced network log filtering
struct NetworkFilterView: View {
    @Binding var filter: NetworkFilter
    let onApply: () -> Void
    
    @Environment(\.dismiss) private var dismiss
    @State private var showFromDatePicker = false
    @State private var showToDatePicker = false
    
    // Available HTTP methods
    private let httpMethods = ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"]
    
    // Status code ranges
    private let statusRanges = ["2xx", "3xx", "4xx", "5xx"]
    
    // Quick preset options
    private let timePresets: [(String, TimeInterval)] = [
        ("Last hour", -3600),
        ("Today", -86400),
        ("Last 24h", -86400),
        ("Last 7 days", -604800)
    ]
    
    var body: some View {
        NavigationView {
            Form {
                // MARK: - HTTP Methods Section
                Section {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(httpMethods, id: \.self) { method in
                                FilterChip(
                                    title: method,
                                    isSelected: filter.selectedMethods.contains(method),
                                    color: .blue
                                ) {
                                    toggleMethod(method)
                                }
                            }
                        }
                        .padding(.vertical, 4)
                    }
                    .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                } header: {
                    Text("HTTP Methods")
                } footer: {
                    Text("Select which HTTP methods to display")
                }
                
                // MARK: - Status Codes Section
                Section {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(statusRanges, id: \.self) { range in
                                FilterChip(
                                    title: range,
                                    isSelected: filter.selectedStatusRanges.contains(range),
                                    color: ColorUtilities.colorForStatusRange(range)
                                ) {
                                    toggleStatusRange(range)
                                }
                            }
                        }
                        .padding(.vertical, 4)
                    }
                    .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                } header: {
                    Text("Status Codes")
                } footer: {
                    Text("Filter by HTTP response status code ranges")
                }
                
                // MARK: - Host/Domain Section
                Section {
                    TextField("Filter by host pattern...", text: $filter.hostPattern)
                        .textFieldStyle(.plain)
                        .autocapitalization(.none)
                        .autocorrectionDisabled()
                } header: {
                    Text("Host / Domain")
                } footer: {
                    Text("Supports wildcards: api.*, *.example.com")
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
                
                // MARK: - Response Time Section
                Section {
                    ForEach(NetworkFilter.ResponseTimeThreshold.allCases, id: \.self) { threshold in
                        Button(action: { toggleResponseTime(threshold) }) {
                            HStack {
                                Image(systemName: filter.responseTimeThreshold == threshold ? "checkmark.circle.fill" : "circle")
                                    .foregroundColor(filter.responseTimeThreshold == threshold ? .orange : .secondary)
                                Text(threshold.rawValue)
                                    .foregroundColor(.primary)
                                Spacer()
                            }
                        }
                    }
                } header: {
                    Text("Response Time")
                } footer: {
                    Text("Filter slow requests by duration threshold")
                }
                
                // MARK: - Failed Requests Toggle
                Section {
                    Toggle("Show only failed requests", isOn: $filter.showOnlyFailed)
                } header: {
                    Text("Errors")
                } footer: {
                    Text("Filter to show only requests with 4xx/5xx status codes or errors")
                }
            }
            .navigationTitle("Network Filters")
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
    
    private func toggleMethod(_ method: String) {
        if filter.selectedMethods.contains(method) {
            filter.selectedMethods.remove(method)
        } else {
            filter.selectedMethods.insert(method)
        }
    }
    
    private func toggleStatusRange(_ range: String) {
        if filter.selectedStatusRanges.contains(range) {
            filter.selectedStatusRanges.remove(range)
        } else {
            filter.selectedStatusRanges.insert(range)
        }
    }
    
    private func toggleResponseTime(_ threshold: NetworkFilter.ResponseTimeThreshold) {
        if filter.responseTimeThreshold == threshold {
            filter.responseTimeThreshold = nil
        } else {
            filter.responseTimeThreshold = threshold
        }
    }
}

// MARK: - Previews

#Preview("NetworkFilterView") {
    NetworkFilterView(
        filter: .constant(NetworkFilter()),
        onApply: {}
    )
}

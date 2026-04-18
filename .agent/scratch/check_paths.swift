import Foundation

let packageDir = "/Users/avilevin/Workspace/Personal/Spectra"
let localCorePath = "build/xcframework/SpectraLogger.xcframework"
let localUIPath = "build/xcframework/SpectraLoggerUI.xcframework"

let absoluteCorePath = packageDir + "/" + localCorePath
let absoluteUIPath = packageDir + "/" + localUIPath

print("Core Path exists: \(FileManager.default.fileExists(atPath: absoluteCorePath))")
print("UI Path exists: \(FileManager.default.fileExists(atPath: absoluteUIPath))")

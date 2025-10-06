Pod::Spec.new do |spec|
  spec.name                     = 'SpectraLogger'
  spec.version                  = '0.0.1'
  spec.homepage                 = 'https://github.com/yourusername/spectra-logger'
  spec.source                   = { :http=> ''}
  spec.authors                  = 'Your Name'
  spec.license                  = 'Apache-2.0'
  spec.summary                  = 'A Kotlin Multiplatform logging framework for mobile applications'
  spec.vendored_frameworks      = 'build/cocoapods/framework/SpectraLogger.framework'
  spec.libraries                = 'c++'
  spec.ios.deployment_target    = '15.0'

  # CocoaPods will build the framework using this custom script
  spec.prepare_command = <<-CMD
    # Determine which target to build based on architecture
    # For local development, we'll build the universal framework

    cd ..

    # Build for simulator (Apple Silicon)
    ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

    # Build for simulator (Intel)
    ./gradlew :shared:linkDebugFrameworkIosX64

    # Build for device
    ./gradlew :shared:linkDebugFrameworkIosArm64

    # Create output directory
    mkdir -p shared/build/cocoapods/framework

    # Copy the simulator arm64 framework as the default
    # (most common for development on Apple Silicon Macs)
    cp -R shared/build/bin/iosSimulatorArm64/debugFramework/SpectraLogger.framework shared/build/cocoapods/framework/
  CMD

  spec.xcconfig = {
    'ENABLE_USER_SCRIPT_SANDBOXING' => 'NO',
  }
end

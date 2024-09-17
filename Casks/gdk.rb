cask 'gdk' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.6.0.1'
    sha256 arm:   '013af768ab22cd8a54993305ea7906d04375f1f46faf43b8179ad77f7e184a97',
           intel: 'dfba64aa5cd59c9cdc9e13f44eb05b2b558b9a8f3c28e1a0efc8234a39613815'

    url "https://github.com/oracle/graal-dev-kit/releases/download/#{version}/gdk-cli-#{version}-macos-#{arch}.tar.gz"
    name 'Graal Development Kit for Micronaut'
    homepage 'https://graal.cloud/gdk/'

    binary "gdk-cli-#{version}-macos-#{arch}/gdk"
    caveats <<~EOS
      On macOS Catalina or later, you may get a warning when you use the Graal Cloud
      Native installation for the first time. This warning can be disabled by running
      the following command:
        sudo xattr -d com.apple.quarantine "#{staged_path}/gdk-cli-#{version}-macos-#{arch}/gdk"

      Graal Development Kit for Micronaut is licensed under the Apache License Version 2.0:
        https://github.com/oracle/graal-dev-kit/blob/main/LICENSE.txt

    EOS
end
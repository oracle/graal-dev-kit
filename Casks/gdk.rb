cask 'gdk' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.6.0.6'
    sha256 arm:   'a60376d84ca3da38eb5aeb05dceeecad1e911d0846a19beda85a1f74481d7d6b',
           intel: '44596a98b3df4d5e992939ecdb25e60232bd4f44fe9b31ec29b8ef25bd31718a'

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
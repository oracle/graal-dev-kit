cask 'gdk' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.6.0.3'
    sha256 arm:   '01c1a3bf96ffc6d14a27c68fd5c6981008846d2b6df7bf2dce10be567057d0c5',
           intel: '06a42eac544f154f3c0f1734a588e469b1ef1fc3fc292b2ca020c93b75a2b8dc'

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
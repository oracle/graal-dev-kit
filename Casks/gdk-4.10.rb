cask 'gdk-4.10' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.10.1.0'
    sha256 arm:   'f4df111295bb162cd8f88af3eee311c0d18d0d9e3e81d139c44fa3b5a8a452fb',
           intel: 'f44e37025b13c4b9bc202602a4bc0970ffb35f7cd0ba7e3b1fb2be50ef64267e'

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

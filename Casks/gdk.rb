cask 'gdk' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.6.0.2'
    sha256 arm:   '61be10bec4f164aab64803f9f9ed1e5e9a8b75c2029e92ae30cf3fb4e5d1cba5',
           intel: '2c6259b700d783abd645eafe7bdb9a23d784c52f90ecc37ecdee5a8b3b97b5d3'

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
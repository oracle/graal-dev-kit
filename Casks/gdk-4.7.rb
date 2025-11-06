cask 'gdk-4.7' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.7.3.8'
    sha256 arm:   '6e2e9bf4578cab153999eb11394430ac5cebb4f5cbc847daf59b9c28cd6e720f',
           intel: 'deab277c26765463b4b82ddd8133582fe95ea9a48be10701b2380836fd97a250'

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

cask 'gcn' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.0.7'
    sha256 arm:   '1acfc2b7a7537e6956603f72ae7c39c95e3b9c7a1574684f63b2cc315e9d770d',
           intel: 'fc1cc35749be9a9ffd2c9e57bfd0665fb6651e9de6ea416f3df00ace4b04109b'

    url "https://github.com/oracle/gcn/releases/download/#{version}/gcn-cli-#{version}-macos-#{arch}.tar.gz"
    name 'Graal Cloud Native'
    homepage 'https://www.graal.cloud/gcn/'

    binary "gcn-cli-#{version}-macos-#{arch}/gcn"
    caveats <<~EOS
      On macOS Catalina or later, you may get a warning when you use the Graal Cloud
      Native installation for the first time. This warning can be disabled by running
      the following command:
        sudo xattr -d com.apple.quarantine "#{staged_path}/gcn-cli-#{version}-macos-#{arch}/gcn"

      Graal Cloud Native is licensed under the Apache License Version 2.0:
        https://github.com/oracle/gcn/blob/main/LICENSE.txt

    EOS
end
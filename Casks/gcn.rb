cask 'gcn' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.3.7.1'
    sha256 arm:   '9e06a6ebc38f1caf95e5864b9bbbd044f6bfe7f69e0f4a518aeebb4ff12f7c12',
           intel: 'b61216e4eda0d8be115241d1826f6903647f9e6df85aa0e60d13e6a9c971ed17'

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
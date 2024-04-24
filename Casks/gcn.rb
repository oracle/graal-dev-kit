cask 'gcn' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.3.7.2'
    sha256 arm:   '8063717c9fe6cb4f8732b3a122a8062069fc76bf08e7f1946078ca290ffabed4',
           intel: 'ee3fd45d5762feea88bc3a61862514ec030ddbc520725d478a1d14d88214799d'

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
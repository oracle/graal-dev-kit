cask 'gcn' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.2.1'
    sha256 arm:   '0ca0b25d760ae1c5faa1bbea4b3174b8ec5d35b4a5012dd584b685b3634b9ff5',
           intel: 'cac348f3f34318d624c99ebc22224354ca44f384e3cc77bda4249d765ebefbe8'

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
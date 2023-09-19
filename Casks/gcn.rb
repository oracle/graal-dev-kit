cask 'gcn' do
    arch arm: 'aarch64', intel: 'amd64'
   
    version '4.0.3'
    sha256 arm:   'f770237c9e9e9335821d43f272dc0cf60cbc9fa083854f4c3360e007db52c3b0',
           intel: 'a5b11ce6d879c89bbee12ec9262860fa8fbb6d8e2b9a84ac32a9f42770a36db1'
  
    url "https://github.com/oracle/gcn/releases/download/#{version}/gcn-cli-#{version}-macos-#{arch}.tar.gz"
    appcast 'https://github.com/oracle/gcn/releases.atom'
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
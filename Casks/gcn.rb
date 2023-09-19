cask 'gcn' do
    arch arm: 'aarch64', intel: 'amd64'
   
    version '3.8.5'
    sha256 arm:   '0f9c278ee7ee1f2e8a3502eb1915b30920acd77c5abcd3d548049210e25cc7bb',
           intel: '01f64f750450f2fc89228b448bd050311cb56c79b9638e4826c9b2637d7543a7'
  
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
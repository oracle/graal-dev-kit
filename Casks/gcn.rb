cask 'gcn' do
    arch arm: 'aarch64', intel: 'amd64'
   
    version '4.0.7'
    sha256 arm:   'fac59e5b8b7c2f593cd258a16c242153251892b1e8e8bbae3309d58fa30a2718',
           intel: '32ebab2e1b6c0f6899a40beeaf09ea45710a0194bcb2ae2168ac9554f6ac1fba'
  
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
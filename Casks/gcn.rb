cask 'gcn' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.2.1-2'
    sha256 arm:   '91c41f0c65382f6866dda2b41c1e3f3406332c76aa214ea3b2869a52943eb896',
           intel: 'ad23ac75c8f5849c60f896c027f97d300f423328ba767ad66bf044b9a2407767'

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
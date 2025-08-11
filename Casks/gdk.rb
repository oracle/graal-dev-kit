cask 'gdk' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.9.1.1'
    sha256 arm:   '9f94699f530d79cb4865050f946a92e9723bec7b058b121c535c5fcbad8b99af',
           intel: 'b9f9453a8aab4b06f8083863a59e67df8c3bdfc534a428d5f04ff8a8b0650781'

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

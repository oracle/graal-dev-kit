cask 'gdk' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.7.3.4'
    sha256 arm:   'ff5b17d7c158fe0aa42b68c9f58f9bd1fce0d552ec91917e8bcbafed04adcde9',
           intel: 'fc9d7e749c51ac3655299245f862a50ddbf4485ddb4e7ac991395e173bd104ca'

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

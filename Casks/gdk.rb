cask 'gdk' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.7.3.2'
    sha256 arm:   '449a11d0dc3596c445bb121810b86394874d3212a70df90efb46e30be3b79208',
           intel: 'c3c460c5a6ff01353be07964d60bd2c40bd09d8a80f8f74ef8c183467542b155'

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

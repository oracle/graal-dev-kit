cask 'gdk-4.10' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.10.1.1'
    sha256 arm:   'bd789603c41ca723d19f4ae4f767cf1dc344b32c5db7706195f88866d9cdfdd4',
           intel: '3cc86f851aeb4252f572d55c5ff16914accb98bf140622d07631835d752e1e14'

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

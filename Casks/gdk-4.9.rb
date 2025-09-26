cask 'gdk-4.9' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.9.1.3'
    sha256 arm:   '6dbd2dcf996407b45c57b99d0c43955d5bd52f0d9c5128e7eeeef158ef3127c6',
           intel: 'eb353f3e08cfe586a1a5ad48fed223cc6731380abcdbaac0d5ac485347071873'

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

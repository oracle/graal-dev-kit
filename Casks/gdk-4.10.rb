cask 'gdk-4.10' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.10.1.3'
    sha256 arm:   '9c6d23939795ebc4c72ba8759ec27dfd34b660aa6ef192d612ba423df6ee202a',
           intel: '98ce7f5fce95001cfb1911ed13c04ca8a71d16fb5372f500f8c338c295a39512'

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

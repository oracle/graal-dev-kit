cask 'gdk' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.5.0.0'
    sha256 arm:   '27e407c5c083b728553a68970db188d6df12a53a4ba2f59b458f4cf4139144f2',
           intel: '46b5df55125cf69bc3c1633f5e7ebdf418a06e377f9631a30251e121a3202db3'

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
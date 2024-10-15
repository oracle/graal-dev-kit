cask 'gdk' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.6.0.3'
    sha256 arm:   '02f4c60235e0bd9c6e8cc28982e7785f6131fcaa1766e68b850ba82653ac6428',
           intel: '003096b853b3fa070a7926c04f2e6d87230ae391f26b962ce72b9be37607395d'

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
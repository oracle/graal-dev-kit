cask 'gdk-4.7' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.7.3.7'
    sha256 arm:   'f679ae099396859f5098e8ce93144c468c57c90f174b9c8944436247a94addde',
           intel: '89ffa8249cf3f9b5209d396d91b7899fffbb5b51d97a0664697f8318623fda2f'

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

cask 'gdk-4.10' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.10.1.2'
    sha256 arm:   '7935d9df9e315909346632ba4df0c4677b9a480161550d4a8638aa3b19462d70',
           intel: 'a80395e0d5f05cd449b0d245beb5fe144e3b68ce71a520c13caf26f050930b7d'

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

cask 'gdk' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.9.1.2'
    sha256 arm:   'f2223ab53bf3e9731136933f6725aaa80d65ce5619a0613a7b70f93ff94b08a4',
           intel: 'f67db8088fbb78b860978339af047843dd7a01b12e7aa1bed978400a68536a4b'

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

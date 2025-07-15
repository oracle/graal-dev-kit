cask 'gdk' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.7.3.5'
    sha256 arm:   '4a70e1f9821f2aa96f01aeae0ca7550ed024d5afdbb8d6c0d4bd6df8a81e88b9',
           intel: 'fd3d2ef2f720b5e3330c1c69ad05a6be1b2e2cf620177c507963aff7ee8db8be'

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

cask 'gdk-4.9' do
    arch arm: 'aarch64', intel: 'amd64'

    version '4.9.1.4'
    sha256 arm:   'e77b5e45fd818553955cdf64915c5497a025d0dcfcc2bc98b725e2c39e684804',
           intel: 'be2dfa61971c0d1a5caac0dc507023a2f77230f640af08f7c33c9ceb845cb597'

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

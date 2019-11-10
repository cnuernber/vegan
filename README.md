# vegan

A nodejs clojurescript library designed to validate and render vega and vega-lite
files.

## Usage

1.  Install nodejs.
2.  I had to [setup](http://npm.github.io/installation-setup-docs/installing/a-note-on-permissions.html) 
	my node modules directory to be under my user home dir as it came preinstalled to 
	a system directory which made the build process fail.
3.  `make prod`
4.  vegan is now installed in your modules.  The odd thing is that it links back
    to this repository so you can never remove this repo...


## Next Steps

* Command line needs improving.  Use a real node cli library, add in support for at
at least scaling chart to different sizes.
* Get the module up on npm as an npm module.  Then the install is npm -i just like 
  anything else.  Maybe then have a public API on it so people can use it in their
  thing.


## License

Copyright © 2019 Chris Nuernberger

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

# vegan

A nodejs clojurescript library designed to validate and render vega and vega-lite
files.

* Please consider [vega-cli](https://www.npmjs.com/package/vega-cli)
* Contributing to [nashorn vega](https://github.com/vega/vega/issues/601)

Currently this does validation and server-side rendering. 

## Installation

1.  Install nodejs.
2.  I had to [setup](http://npm.github.io/installation-setup-docs/installing/a-note-on-permissions.html) 
	my node modules directory to be under my user home dir as it came preinstalled to 
	a system directory which made the build process fail.
3.  `make prod`
4.  vegan is now installed in your modules.  The odd thing is that it links back
    to this repository so you can never remove this repo...
	
	
## Usage

```console
Arguments:
-v, --validate fname - validate vega file indicated by fname.
-r --render src-file dst-file - render vega to a png, svg, jpg, or pdf file.
```

* ![pdf output](images/bar-chart.pdf)
* ![png output](images/bar-chart.png)
* ![jpeg output](images/bar-chart.jpg)
* ![svg output](images/bar-chart.svg)


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

SOURCEDIR := "src"
BUILD_SOURCES := project.clj Makefile node/package.json externs.js
CLJS_SOURCES := $(shell find $(SOURCEDIR) -name '*.cljs')
CLJC_SOURCES := $(shell find $(SOURCEDIR) -name '*.cljc')
JSON_RESOURCES := $(shell find "resources" -name '*.json')
MODULEDIR := "$(shell pwd)/node_modules"
VEGAN := $(MODULEDIR)/bin/vegan
FINAL_RESOURCEDIR := "node/out/vegan/resources"
RELEASE_RESOURCEDIR := "prod/resources"

all: $(VEGAN)

node/app.js: $(CLJS_SOURCES) $(CLJC_SOURCES) $(BUILD_SOURCES) $(JSON_RESOURCES)
	rm -f node/app.js
	lein cljsbuild once app
	mkdir -p $(FINAL_RESOURCEDIR) && cp $(JSON_RESOURCES) $(FINAL_RESOURCEDIR)

$(VEGAN): node/app.js
	cd node && NPM_CONFIG_PREFIX=$(MODULEDIR) npm link . && cd ..


prod: $(CLJS_SOURCES) $(CLJC_SOURCES) $(BUILD_SOURCES) $(JSON_RESOURCES)
	rm -rf prod/app.js
	lein cljsbuild once prod
	mkdir -p $(RELEASE_RESOURCEDIR) && cp $(JSON_RESOURCES) $(RELEASE_RESOURCEDIR)
	cp node/package.json prod/package.json
	rm -rf prod/out
	cd prod && npm link . && cd ..


install: $(VEGAN)
	cd node && npm install && cd ..

clean:
	rm -rf node/app.js node/out $(VEGAN)

#!/bin/sh
sudo apt-get install -y libblosc1
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/main/ci-setup-github-actions.sh
sh ci-setup-github-actions.sh

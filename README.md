[![Build Status](https://github.com/mobie/mobie-io/actions/workflows/build.yml/badge.svg)](https://github.com/mobie/mobie-io/actions/workflows/build.yml)

# mobie-io

Collection of classes to load different file formats into BigDataViewer or write data into a BigDataViewer compatible
format. Currently supports:

- [n5](https://github.com/saalfeldlab/n5)
- [ome.zarr](https://ngff.openmicroscopy.org/latest/)
- [open organelle](https://openorganelle.janelia.org/) (experimental)

Makes use of the [saalfeldlab n5 stack](https://github.com/saalfeldlab). For a Fiji plugin to open ome.zarr files in
BigDataViewer, please [install MoBIE](https://github.com/mobie/mobie-viewer-fiji#install), which will
install `Plugins->BigDataViewer->OME ZARR` in your Fiji.


## Citation

If you use mobie.io as a library and use it in your research, please cite 
[Pape, C., Meechan, K., Moreva, E. et al. MoBIE: a Fiji plugin for sharing and exploration of multi-modal cloud-hosted big image data. Nat Methods (2023). https://doi.org/10.1038/s41592-023-01776-4](https://www.nature.com/articles/s41592-023-01776-4).

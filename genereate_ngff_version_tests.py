import os
import sys


def generate_ngff_version_tests(version):

    folder = f"./src/test/java/projects/ngff/v0{version}"
    os.makedirs(folder, exist_ok=True)

    test_names = {
        "CYX": "cyx",
        "CZYX": "czyx",
        "MultiImage": "multi-image",
        "TCYX": "tcyx",
        "TCZYX": "tczyx",
        "TYX": "tyx",
        "YX": "yx",
        "ZYX": "zyx",
    }

    for name, url_name in test_names.items():

        # multi-image is only there for v>=0.4
        if name == "MultiImage" and version < 4:
            continue

        # czyx is only there for v>=0.3
        if name == "CZYX" and version < 3:
            continue

        test = f"""package projects.ngff.v0{version};

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import projects.ngff.base.{name}NgffBaseTest;

@Slf4j
public class {name}NgffV0{version}Test extends {name}NgffBaseTest{{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.{version}/{url_name}.ome.zarr";
    public {name}NgffV0{version}Test() throws SpimDataException {{
        super(URL);
    }}
}}"""
        test_file = os.path.join(folder, f"{name}NgffV0{version}Test.java")
        with open(test_file, "w") as f:
            f.write(test)


if __name__ == "__main__":
    generate_ngff_version_tests(int(sys.argv[1]))

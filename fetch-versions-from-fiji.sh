#!/bin/bash

# Directories containing the JAR files
jars_dir="/Users/tischer/Desktop/Fiji/Fiji.app/jars"
plugins_dir="/Users/tischer/Desktop/Fiji/Fiji.app/plugins"

# Prefix to filter jar files by
prefix="n5"

# Temporary file to hold the versions
temp_file=$(mktemp)

# Function to extract artifactId and version from a jar file name
extract_info() {
    jar_name=$(basename "$1")
    # Check if the file starts with the desired prefix
    if [[ "$jar_name" == $prefix* ]]; then
        artifactId=$(echo "$jar_name" | sed -E 's/([^-]+)-([0-9].*)\.jar/\1/')
        version=$(echo "$jar_name" | sed -E "s/${artifactId}-((.*))\.jar/\1/")
        echo "<${artifactId}.version>${version}</${artifactId}.version>" >> "$temp_file"
    fi
}

export -f extract_info
export temp_file
export prefix

# Process jars and plugins directories
find "$jars_dir" "$plugins_dir" -type f -name "${prefix}*.jar" -exec bash -c 'extract_info "$0"' {} \;

# Sort and remove duplicates
sort "$temp_file" | uniq

# Remove the temporary file
rm "$temp_file"

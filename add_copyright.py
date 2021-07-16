import sys
import os
import yaml

root_path = sys.argv[1]

if root_path is None:
	raise Exception("Specify root path!")

config_path = root_path + "/copyright_config.yml"
src_path = root_path + "/src"

print("Config Path: " + config_path)
print("Src Path: " + src_path)

with open(config_path, "r") as ymlfile:
	config = yaml.load(ymlfile, Loader=yaml.FullLoader)

config_section_license = config["license"]
config_section_program = config["program"]

license_name = config_section_license["name"]
license_version = config_section_license["version"]
program_name = config_section_program["name"]
program_description = config_section_program["description"]
program_authors = config_section_program["authors"]
program_url = config_section_program["url"]
copyright_year = config_section_program["year"]


with open("copyright-" + license_name, "r") as f:
	copyright_text = f.read()
	copyright_text = copyright_text.format(
		version = license_version,
		program = program_name,
		description = program_description,
		authors = program_authors,
		url= program_url,
		year = copyright_year
	)
print("Copyright Text")
print(copyright_text)


def read_file(file_name):
	with open(file_name, "r") as f:
		read_lines = f.readlines()
	return read_lines


def write_java_file(file_name, original_lines):
	with open(file_name, "w+") as f:
		start = False
		for line in original_lines:
			if (not start) and (line.startswith("package ") or line.startswith("module ")):
				start = True
				split_lines = copyright_text.splitlines(True)
				f.write("/* " + split_lines[0])
				for copyright_line in split_lines[1:len(split_lines)]:
					f.write(" * " + copyright_line)
				f.write(" */\n")
			if start:
				f.write(line)


print("Files:")
for (dir_path, dir_names, file_names) in os.walk(src_path):
	for file_name in file_names:
		if str(file_name).endswith(".java"):
			file_path = dir_path + "/" + file_name
			print(file_path)
			lines = read_file(file_path)
			write_java_file(file_path, lines)


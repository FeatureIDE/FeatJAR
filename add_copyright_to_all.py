import sys
import os
from pathlib import Path
import yaml
import xml.etree.ElementTree as xml

root_path = Path(".")
root_information_path = root_path.joinpath("copyright_config.yml")

with open(root_information_path, "r") as ymlfile:
	root_information = yaml.load(ymlfile, Loader=yaml.FullLoader)


def add_to_project(project_path):
	print(str(project_path))
	pom_path = project_path.joinpath("pom.xml")
	config_path = project_path.joinpath("copyright_config.yml")
	src_path = project_path.joinpath("src")

	if config_path.exists():
		with open(config_path, "r") as ymlfile:
			config = yaml.load(ymlfile, Loader=yaml.FullLoader)
			information = root_information | config
	else:
		information = root_information

	root = xml.parse(pom_path).getroot()
	
	information['name'] = pom_value(root, 'name')
	information['description'] = pom_value(root, 'description')
	information['url'] = pom_value(root, 'url')

	copyright_text = create_copyright_text(information)
	modify_source_files(src_path, copyright_text)


def pom_value(root, name):
	maven_prefix = '{http://maven.apache.org/POM/4.0.0}'
	element = root.find(maven_prefix + name)
	if element is None:
		return ''
	else:
		return element.text


def create_copyright_text(information):
	print("Copyright Text:")
	with open("copyright-" + information['license_name'], "r") as f:
		copyright_text = f.read()
		copyright_text = copyright_text.format(
			version = information['license_version'],
			program = information['name'],
			description = information['description'],
			authors = information['authors'],
			url= information['url'],
			year = information['year']
		)
	print(copyright_text)
	return copyright_text


def modify_source_files(src_path, copyright_text):
	print("Files:")
	for (dir_path, dir_names, file_names) in os.walk(src_path):
		for file_name in file_names:
			if str(file_name).endswith(".java"):
				file_path = dir_path + "/" + file_name
				print(file_path)
				lines = read_file(file_path)
				write_java_file(file_path, lines, copyright_text)


def read_file(file_name):
	with open(file_name, "r") as f:
		read_lines = f.readlines()
	return read_lines


def write_java_file(file_name, original_lines, copyright_text):
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


dirs =[d for d in root_path.iterdir() if d.is_dir() and d.joinpath("pom.xml").exists() and d.joinpath('src').exists()]
for d in dirs:
	add_to_project(d)

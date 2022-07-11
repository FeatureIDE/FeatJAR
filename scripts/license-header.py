# This script adds a license header to all Java source files.
# The required information is read from the Maven POM file and Git.
# Prerequisite: sudo apt install python3 python3-pip && python3 -mpip install GitPython

import sys
import os
import time
from pathlib import Path
import yaml
import xml.etree.ElementTree as xml
import git

root_path = Path(os.path.dirname(os.path.realpath(sys.argv[0])) + "/..")

def add_license_header_to_project(project_path):
	print(str(project_path))
	pom_path = project_path.joinpath("pom.xml")
	src_path = project_path.joinpath("src")
	information = {}

	root = xml.parse(pom_path).getroot()
	ns = {"maven": "http://maven.apache.org/POM/4.0.0"}
	repo = git.Repo(project_path)
	first_commit = [commit for commit in repo.iter_commits(rev=repo.head.reference)][-1]
	url = repo.remotes.origin.url.replace("git@github.com:", "https://github.com/").replace(".git", "")

	information['name'] = pom_value(root, 'name') or project_path.name
	information['description'] = pom_value(root, 'description')
	information['url'] = pom_value(root, 'url') or url
	information['year'] = pom_value(root, 'inceptionYear') or time.gmtime(first_commit.authored_date).tm_year
	information['author'] = ", ".join([e.text for e in root.findall('.//maven:developers/maven:developer/maven:name', ns)]) or first_commit.author.name
	information['license'] = root.find('.//maven:licenses/maven:license/maven:comments', ns)
	information['license'] = information['license'].text if information['license'] is not None else 'lgpl30'
	copyright_text = create_copyright_text(information)
	modify_source_files(src_path, copyright_text)

def pom_value(root, name):
	maven_prefix = '{http://maven.apache.org/POM/4.0.0}'
	element = root.find(maven_prefix + name)
	return element.text if element is not None else ''


def create_copyright_text(information):
	with open(str(root_path) + "/scripts/license-header-" + information['license'] + ".txt", "r") as f:
		copyright_text = f.read()
		copyright_text = copyright_text.format(
			program = information['name'],
			description = information['description'],
			author = information['author'],
			url= information['url'],
			year = information['year']
		)
	return copyright_text


def modify_source_files(src_path, copyright_text):
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
	add_license_header_to_project(d)

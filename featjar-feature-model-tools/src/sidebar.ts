import * as vscode from 'vscode';

class UvlFileProvider implements vscode.TreeDataProvider<vscode.Uri> {

    getTreeItem(element: vscode.Uri): vscode.TreeItem {
        return new vscode.TreeItem(element);
    }

    async getChildren(): Promise<vscode.Uri[]> {
        const files = await vscode.workspace.findFiles('**/*.uvl');
        return files;
    }
}

export function registerSidebar(context: vscode.ExtensionContext) {

    const uvlFileProvider = new UvlFileProvider();

    const uvlTree = vscode.window.registerTreeDataProvider(
        'uvlFiles',
        uvlFileProvider
    );

    context.subscriptions.push(uvlTree);
}
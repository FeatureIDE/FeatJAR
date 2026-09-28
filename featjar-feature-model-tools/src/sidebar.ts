import * as vscode from 'vscode';

class UvlFileProvider implements vscode.TreeDataProvider<vscode.Uri> {
    //die private Variable __onDidChangeTreeData ist von AI vorgeschlagen. Damit ich die regelmäßige Refresh benutzen kann.
    private _onDidChangeTreeData =new vscode.EventEmitter<vscode.Uri | undefined>();

    readonly onDidChangeTreeData =this._onDidChangeTreeData.event;

    getTreeItem(element: vscode.Uri): vscode.TreeItem {
        return new vscode.TreeItem(element);
    }

    async getChildren(): Promise<vscode.Uri[]> {
        const files = await vscode.workspace.findFiles('**/*.uvl');
        return files;
    }
    refresh(): void {
        this._onDidChangeTreeData.fire(undefined);
    }
}

export function registerSidebar(context: vscode.ExtensionContext) {

    const uvlFileProvider = new UvlFileProvider();
    const refreshCommand = vscode.commands.registerCommand('featjar-extension.refreshUvlFiles',() => {
        uvlFileProvider.refresh();
    }
    );
    const uvlTree = vscode.window.registerTreeDataProvider(
        'uvlFiles',
        uvlFileProvider
    );

    context.subscriptions.push(uvlTree, refreshCommand);
}
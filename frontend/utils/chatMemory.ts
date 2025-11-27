import { ChatTreeNode, MemoryScopePayload } from "@/types/chat";

function findNode(
  nodes: ChatTreeNode[],
  targetId: string,
  ancestors: ChatTreeNode[] = []
): { node: ChatTreeNode; ancestors: ChatTreeNode[] } | null {
  for (const node of nodes) {
    if (node.id === targetId) {
      return { node, ancestors };
    }
    if (node.children?.length) {
      const found = findNode(node.children, targetId, [...ancestors, node]);
      if (found) return found;
    }
  }
  return null;
}

function collectSessions(node: ChatTreeNode): string[] {
  const result: string[] = [];
  const stack: ChatTreeNode[] = [node];
  while (stack.length) {
    const current = stack.pop()!;
    if (current.type === "session") {
      result.push(current.id);
    }
    if (current.children?.length) {
      stack.push(...current.children);
    }
  }
  return result;
}

/**
 * Calculate the memory scope given the chat tree and active node id.
 */
export function computeMemoryScope(
  tree: ChatTreeNode[],
  activeNodeId: string
): MemoryScopePayload {
  const found = findNode(tree, activeNodeId);
  if (!found) {
    return { sessionId: activeNodeId, memorySessionIds: [] };
  }

  const { node, ancestors } = found;
  if (node.type === "group") {
    return {
      sessionId: node.id,
      memorySessionIds: collectSessions(node),
    };
  }

  // active node is session
  const sessionId = node.id;
  const memorySessionIds = new Set<string>([sessionId]);

  const groupsInPath = ancestors.filter((item) => item.type === "group");
  for (const group of groupsInPath) {
    collectSessions(group).forEach((id) => memorySessionIds.add(id));
  }

  return { sessionId, memorySessionIds: Array.from(memorySessionIds) };
}

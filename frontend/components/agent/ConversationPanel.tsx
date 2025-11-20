'use client';

import clsx from 'clsx';
import ReactMarkdown from 'react-markdown';
import { BookOpen, MessageSquare, Sparkles } from 'lucide-react';
import { MutableRefObject } from 'react';

import GraphRenderer from '@/components/GraphRenderer';
import PlanTimeline, { PlanStep } from '@/components/PlanTimeline';

import type {
  AgentDocumentSnippet,
  AgentGraphPayload,
  ConversationMessage,
} from './types';

interface ConversationPanelProps {
  anchorRef: MutableRefObject<HTMLDivElement | null>;
  messages: ConversationMessage[];
  isLoading: boolean;
  plan: PlanStep[];
  graph: AgentGraphPayload | null;
  documents: AgentDocumentSnippet[];
}

export default function ConversationPanel({
  anchorRef,
  messages,
  isLoading,
  plan,
  graph,
  documents,
}: ConversationPanelProps) {
  const hasInsights = plan.length > 0 || graph || documents.length > 0;

  return (
    <section className="conversation-panel" ref={anchorRef}>
      <div className="message-feed">
        {messages.map((message) => (
          <div
            key={message.id}
            className={clsx('message-row', {
              'message-row--right': message.role === 'user',
            })}
          >
            <div
              className={clsx('message-bubble', {
                'message-bubble--user': message.role === 'user',
                'message-bubble--assistant': message.role === 'assistant',
              })}
            >
              <ReactMarkdown className="prose prose-sm max-w-none">
                {message.content}
              </ReactMarkdown>
            </div>
          </div>
        ))}

        {isLoading && (
          <div className="message-row message-row--assistant">
            <div className="message-bubble message-bubble--assistant">
              <div className="typing-indicator">
                <span />
                <span />
                <span />
              </div>
              <p className="typing-label">智能体正在思考...</p>
            </div>
          </div>
        )}
      </div>

      {hasInsights && (
        <div className="insight-grid">
          {plan.length > 0 && (
            <article className="insight-card">
              <header className="insight-card__header">
                <Sparkles className="h-4 w-4 text-amber-500" />
                <span>思维步骤</span>
              </header>
              <PlanTimeline steps={plan} />
            </article>
          )}

          {graph && (
            <article className="insight-card">
              <header className="insight-card__header">
                <MessageSquare className="h-4 w-4 text-amber-500" />
                <span>图谱视图</span>
              </header>
              <GraphRenderer graph={graph} />
            </article>
          )}

          {documents.length > 0 && (
            <article className="insight-card">
              <header className="insight-card__header">
                <BookOpen className="h-4 w-4 text-amber-500" />
                <span>上下文文档</span>
              </header>
              <div className="document-list">
                {documents.map((doc) => (
                  <div key={doc.documentId} className="document-list__item">
                    <div>
                      <p className="document-title">{doc.title}</p>
                      <p className="document-summary">{doc.summary}</p>
                    </div>
                    <span className="document-source">{doc.source}</span>
                  </div>
                ))}
              </div>
            </article>
          )}
        </div>
      )}
    </section>
  );
}

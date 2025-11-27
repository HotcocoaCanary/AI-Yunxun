'use client';
import ProjectHero from "@/components/index/ProjectHero";
import GlobalGraphOverview from "@/components/index/GlobalGraphOverview";
import FeatureHighlights from "@/components/index/FeatureHighlights";

export default function HomePage() {
  return (
    <main className="page-shell home-shell">
      <ProjectHero />
      <GlobalGraphOverview />
      <FeatureHighlights />
    </main>
  );
}

import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";

interface RankingUser {
  id: number;
  rank: number;
  name: string;
  username: string;
  avatar: string;
  points: number;
  trend: number; // Positive = up, negative = down, 0 = no change
  trendDirection: "up" | "down" | "neutral";
}

type FilterPeriod = "weekly" | "monthly" | "alltime";

@Component({
  selector: "app-trust-rankings",
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: "./trust-rankings.component.html",
  styleUrl: "./trust-rankings.component.scss",
})
export class TrustRankingsComponent implements OnInit {
  Math = Math; // Expose Math to template
  searchQuery = "";
  selectedPeriod: FilterPeriod = "weekly";

  // Current user stats
  currentUserRank = 42;
  currentUserPoints = 850;
  currentUserTrend = 3;
  currentUserAvatar =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuApY2AgTXAuEuMlbfxeIrXJCxMp2rJ_xS6S4OxcV_g2xI-hfusBW5RNrHJZUkDrYpHSAlAARpihJQ0ST9alpT1rrsRljPYL_2yrV7N0fd5H9yfo5LupXO25-c8eMORfydVCAU2DBgqC9ANlwA1588CJhUZLzWDxxtNfIee49_FsarosR8soY85nFzeG8Y5RL-EYzKvljRcAILSeTgFgWnyt3GZINHH-9EhEofdVeDZCzIFhNhCpc1PKv7AQKaB-YTWvsZbUiWdG3ww";

  // Mock rankings data
  rankings: RankingUser[] = [
    {
      id: 1,
      rank: 1,
      name: "Sarah Jenkins",
      username: "@sarah_jen",
      avatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuAY4WuG2pqehAxck6qv4JifZCsNUgirc9nintLrDUyqmkVkizUa1JZJaI7ouKCB-i03sMKqnxzcD4n5rbHljJ1MJ2pFgY30jyNWQSgGYhkat8ObzODPqzcmhMZNJ4VzqvjUgJAavWLRW7OxPw16NCPIYALHjHrSw7PGNroeEj5UniAPqZeyKxmzrlUWaf2wGIG4KPFcFpZXJlc5uFcwWABUs_pV-DyMUtnAdz8DARXRti6ic9KGyicxP6JSBUX2LHPcn5Vi-AV1D74",
      points: 1500,
      trend: 0,
      trendDirection: "neutral",
    },
    {
      id: 2,
      rank: 2,
      name: "Michael O.",
      username: "@michael_o",
      avatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBV1t5tXZ37lVJ9uvpaABfdm-I139_AdE85TuLSvR5kQp9UkOrn5m-BbQ8K7x0s_-NkbwxbYXiN_AJlmEGcvUlTbqpneOIKgrgW2BVg94ndeVOAhSlaAgAPB135Sk3F2kCknhpCJ0a2FQxDWIBy4Ol3KAug00b7XwUVBKcC7xJZCxNwWd7uJU0oEIN_zGdMZSSyPiUfWa3hNxlsXQHtRS9Du5-bQRiyUQVwAPzVhsr62aD_lGOXpDqPyAy1Yr9Ux_HqsCeKkIa_fOs",
      points: 1320,
      trend: 5,
      trendDirection: "up",
    },
    {
      id: 3,
      rank: 3,
      name: "David K.",
      username: "@david_k",
      avatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDaBGS3j-JefAnqgPqdFJftevVCmc_wz1J0nLtQhrzI4ej6D3jnp-82y6ackiDNpLbP6k1Px5-S9iHbA0nRfrmOhZPPjeyT8fufrrbC7TLYqkvjsBHG32RwFx7RTrhEFyDn4MqKfxu2UCdLgGRj4h6zUH_8e_cAwOEs1LZqh6DJ6C7HSxHo9haeNDrnPiI7OWa0XoU5cDMSi08F9ChzntPPRtO9rMMDg2Ca61yNgzKPk_TyqAaSAOylWHqoU7cQjBkA2ptHF0VSIYQ",
      points: 1250,
      trend: 12,
      trendDirection: "up",
    },
    {
      id: 4,
      rank: 4,
      name: "Emily R.",
      username: "@emily_saves",
      avatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuCnu7ZcRrbwvxoL_tWKNoruZVaCFo6N2Fft2iI2rkR30mMqplzlAkt2s8wqQTGHbcbdN4malXO5dF4FK6-hUAjI-KaIl9CXg0TJ6KjLri1W3Z40EkaZgG_3BH09Jeh8fNTD5aotVnKAtZxjQw-jmIk6cMu-M169C9wATqlStDoHB98bysXjhUr4tJVxXuqLD7_DZ11apVPVDEF2R1Cny5pMeXvhynx18sb3Oy3DU3XGuXWyRUPonOZy8Uj8ho-BQ7YstFluKmLiOmk",
      points: 1150,
      trend: 24,
      trendDirection: "up",
    },
    {
      id: 5,
      rank: 5,
      name: "Marcus T.",
      username: "@mt_investor",
      avatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBeH6MSsN_rvUAygebufgcEeruJIadITu0I5NsohOT9NqXKodOOxzI_Omyp9dtjgfY58bPKDw4Mge92S3uTKPxLDdRNOC18ZcOfZB_1sjwPEAB5MWmfo10_gkMAy-_BR8vHETOfVI3yU-z4tyzQRO9nhdyRJyjWbDuCjxYDzyppiEOYMxU-kFBOcTxTeVQQvemHQ8VSX_L8NFDHSKIGV4JB5RvO4hesXR_U0jaYsW7HY46nHB61rP6-snVxFxGOJ0Kab1yaUtEtmCM",
      points: 1080,
      trend: -5,
      trendDirection: "down",
    },
    {
      id: 6,
      rank: 6,
      name: "Aisha B.",
      username: "@aisha_b",
      avatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDiEtr1asw05kpHw65Us_tzzWTGrRwU0CWg2vH_zKeQFqFUZWdxDdrgQchl5h8-7MNG6gqMABZ1RD9TQsSwyvvVO1aP9dBgx_fLWI6ATfCBQulBm5FOOexsGEAo5zbeMYlY5Qmv4-i2nh4Jd-W1QFKjwOH9hAXDg3DxK2_9q7YeUFZ4Eow3zNAkbE-NebYHIAYYhF2HEGfLAFPmTJhnpen-69CubUJk_5jJMdz-c1EByRncNh76waNglP3TYOwlNwQHhlUneEhtNYk",
      points: 995,
      trend: 0,
      trendDirection: "neutral",
    },
    {
      id: 7,
      rank: 7,
      name: "John D.",
      username: "@jd_saver",
      avatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuAjdw_3l3xNprU2k9imQypuobWbnGd7NjIMZq8TltC0ucmthOTMlEYiiHfIrG4fSUPG683eoqp4f0G8O4VEbo5-NSzyJm0WTjHZRXwgh8brzTfqd3QG-EF28M_uMQW-EZXfkcysjp_d6SKtRWuNdkeVgs9Ptd5IgFSv0Q0cFisd6exHEQsujobnpKTqj-gLXGrUROaLZOAKupTwS6qivSq0YvxmuN4Y7yIwwqL3iF6F6oiP5zabEP2HzJ-_TlEUA5_i-MlDqtB1hGI",
      points: 940,
      trend: 12,
      trendDirection: "up",
    },
  ];

  constructor(private router: Router) {}

  ngOnInit(): void {
    // TODO: Load rankings from service
  }

  get topThree(): RankingUser[] {
    return this.rankings.slice(0, 3);
  }

  get remainingRankings(): RankingUser[] {
    return this.filteredRankings.slice(3);
  }

  get filteredRankings(): RankingUser[] {
    let filtered = this.rankings;

    // Filter by search query
    if (this.searchQuery) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(
        (user) =>
          user.name.toLowerCase().includes(query) ||
          user.username.toLowerCase().includes(query),
      );
    }

    return filtered;
  }

  setPeriod(period: FilterPeriod): void {
    this.selectedPeriod = period;
    // TODO: Load rankings for selected period from API
    console.log("Period changed to:", period);
  }

  onSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchQuery = input.value;
  }

  viewUserProfile(userId: number): void {
    console.log("View profile for user:", userId);
    // TODO: Navigate to user profile or show modal
  }

  viewMyStats(): void {
    console.log("View my stats");
    // TODO: Navigate to user's detailed stats page
    this.router.navigate(["/dashboard/client"]);
  }

  loadMoreRankings(): void {
    console.log("Load more rankings");
    // TODO: Load next page of rankings from API
  }

  getRankBadgeColor(rank: number): string {
    switch (rank) {
      case 1:
        return "border-[#FFD700]"; // Gold
      case 2:
        return "border-slate-400"; // Silver
      case 3:
        return "border-[#CD7F32]"; // Bronze
      default:
        return "border-gray-300 dark:border-gray-700";
    }
  }

  getTrendIcon(direction: string): string {
    switch (direction) {
      case "up":
        return "trending_up";
      case "down":
        return "trending_down";
      default:
        return "remove";
    }
  }

  getTrendColor(direction: string): string {
    switch (direction) {
      case "up":
        return "text-primary";
      case "down":
        return "text-red-500";
      default:
        return "text-gray-500 dark:text-gray-400";
    }
  }

  formatPoints(points: number): string {
    return points.toLocaleString();
  }
}

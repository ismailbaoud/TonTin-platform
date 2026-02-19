import { Component, OnInit, OnDestroy } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ActivatedRoute, Router, RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { Subject, takeUntil, finalize } from "rxjs";
import { DarService } from "../services/dar.service";
import { RoundService } from "../services/round.service";
import { AuthService } from "../../../../auth/services/auth.service";
import { MemberStatus } from "../enums/member-status.enum";
import { Round, RoundStatus, formatRoundDate, getRelativeTime, getRoundStatusLabel, getRoundStatusColor } from "../models/round.model";

interface Member {
  id: string;
  name: string;
  email: string;
  avatar: string;
  role: string;
  status: MemberStatus; // Member participation status (PENDING/ACTIVE/LEAVED)
  turnDate: string;
  // NOTE: paymentStatus removed - should be tracked separately in a Payment entity
  // For now, we'll use a placeholder based on member status for display purposes only
}

interface DarDetails {
  id: string; // UUID
  name: string;
  image: string;
  status: "active" | "completed" | "pending" | "finished";
  organizer: string;
  startDate: string;
  currentCycle: number;
  totalCycles: number;
  progress: number;
  totalMembers: number;
  monthlyPot: number;
  nextPayout: string;
  members: Member[];
}

@Component({
  selector: "app-dar-details",
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: "./dar-details.component.html",
  styleUrl: "./dar-details.component.scss",
})
export class DarDetailsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // UI State
  activeTab: "members" | "tours" | "messages" | "settings" = "members";
  searchQuery = "";
  darId: string | null = null; // UUID from route params
  isLoading = false;
  error: string | null = null;

  // Invite modal state
  showInviteModal = false;
  inviteSearchQuery = "";
  searchResults: Array<{
    id: string;
    name: string;
    email: string;
    avatar: string | null;
  }> = [];
  isSearching = false;
  invitingUserId: string | null = null;

  // Data
  darDetails: DarDetails | null = null;
  isOrganizer = false;
  rounds: Round[] = [];
  currentRound: Round | null = null;
  isLoadingRounds = false;

  // No more mock data - using real API

  // Static mock data (easy to replace with API later)
  // Mock data removed - we only use real API data now

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private darService: DarService,
    private roundService: RoundService,
  ) {}

  ngOnInit(): void {
    // Subscribe to route params changes to reload data when navigating between darts
    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      this.darId = params.get("id");
      console.log(
        "╔═══════════════════════════════════════════════════════════╗",
      );
      console.log(
        "║  DART DETAILS COMPONENT - ROUTE PARAMS CHANGED           ║",
      );
      console.log(
        "╚═══════════════════════════════════════════════════════════╝",
      );
      console.log("🆔 New Dart ID from route:", this.darId);
      console.log("📋 Full route params:", params);
      console.log("🌐 Current URL:", window.location.href);
      console.log("🔄 Component will reload data for this dart");

      // Reset state
      this.darDetails = null;
      this.error = null;

      // Load data for the new dart
      this.loadDarDetails();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load Dâr details
   * Currently uses mock data, but structured to easily replace with API call
   */
  loadDarDetails(): void {
    if (!this.darId) {
      this.error = "No Dâr ID provided";
      console.error(
        "╔═══════════════════════════════════════════════════════════╗",
      );
      console.error(
        "║  ❌ ERROR: NO DART ID FOUND                             ║",
      );
      console.error(
        "╚═══════════════════════════════════════════════════════════╝",
      );
      return;
    }

    console.log(
      "╔═══════════════════════════════════════════════════════════╗",
    );
    console.log("║  📡 LOADING DART DETAILS FROM API                        ║");
    console.log(
      "╚═══════════════════════════════════════════════════════════╝",
    );
    console.log(`🆔 Dart ID to fetch: ${this.darId}`);
    console.log(`🌐 API URL: ${this.darService["apiUrl"]}/${this.darId}`);
    this.isLoading = true;
    this.error = null;

    this.darService
      .getDarDetails(this.darId)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isLoading = false)),
      )
      .subscribe({
        next: (data) => {
          console.log(
            "╔═══════════════════════════════════════════════════════════╗",
          );
          console.log(
            "║  ✅ DART DETAILS LOADED FROM API                         ║",
          );
          console.log(
            "╚═══════════════════════════════════════════════════════════╝",
          );
          console.log("📦 RAW API RESPONSE:");
          console.log("  🆔 ID:", data.id);
          console.log("  📝 Name:", data.name);
          console.log("  📊 Status:", data.status);
          console.log("  👤 Organizer Name:", data.organizerName);
          console.log("  👥 Member Count:", data.memberCount);
          console.log("  💰 Monthly Contribution:", data.monthlyContribution);
          console.log("  📅 Start Date:", data.startDate);
          console.log("  🔄 Current Cycle:", data.currentCycle);
          console.log("  🎯 Total Cycles:", data.totalCycles);
          console.log("  🖼️  Image:", data.image);
          console.log("  📋 Full Response Object:", data);

          console.log("\n🔄 MAPPING API DATA TO COMPONENT FORMAT...");
          this.darDetails = this.mapApiDataToComponent(data);

          console.log(
            "╔═══════════════════════════════════════════════════════════╗",
          );
          console.log(
            "║  📊 MAPPED DATA READY FOR DISPLAY                        ║",
          );
          console.log(
            "╚═══════════════════════════════════════════════════════════╝",
          );
          console.log("  🆔 darDetails.id:", this.darDetails.id);
          console.log("  📝 darDetails.name:", this.darDetails.name);
          console.log("  👤 darDetails.organizer:", this.darDetails.organizer);
          console.log(
            "  👥 darDetails.totalMembers:",
            this.darDetails.totalMembers,
          );
          console.log(
            "  💰 darDetails.monthlyPot:",
            this.darDetails.monthlyPot,
          );
          console.log("  📊 darDetails.status:", this.darDetails.status);
          console.log("  📋 Full darDetails Object:", this.darDetails);

          // Check if current user is organizer
          this.isOrganizer = data.isOrganizer || false;
          console.log("  👤 Is Organizer:", this.isOrganizer);

          // Load members separately
          this.loadMembers();

          // Load rounds if dart is active (check both uppercase and lowercase)
          const status = data.status?.toUpperCase() || '';
          console.log("🔍 Dart status check for rounds loading:", {
            rawStatus: data.status,
            upperCaseStatus: status,
            willLoadRounds: status === 'ACTIVE'
          });
          if (status === 'ACTIVE') {
            this.loadRounds();
          } else {
            console.log("⏸️ Dart is not active, skipping rounds load. Status:", status);
          }
        },
        error: (err) => {
          console.error(
            "╔═══════════════════════════════════════════════════════════╗",
          );
          console.error(
            "║  ❌ ERROR LOADING DART DETAILS                           ║",
          );
          console.error(
            "╚═══════════════════════════════════════════════════════════╝",
          );
          console.error("🆔 Attempted Dart ID:", this.darId);
          console.error(
            "🌐 API URL:",
            `${this.darService["apiUrl"]}/${this.darId}`,
          );
          console.error("📊 Status Code:", err.status);
          console.error("📋 Error Message:", err.error?.message);
          console.error("📦 Full Error Object:", err);
          console.error("⚠️  THIS MEANS THE API CALL FAILED - NO DATA LOADED");

          this.error =
            err.error?.message ||
            "Failed to load Dâr details. Please try again.";
          // Don't fallback to mock data - show error instead
        },
      });
  }

  /**
   * Load members for the dart
   */
  private loadMembers(): void {
    if (!this.darId) return;

    console.log(
      "╔═══════════════════════════════════════════════════════════╗",
    );
    console.log("║  👥 LOADING MEMBERS FROM API                             ║");
    console.log(
      "╚═══════════════════════════════════════════════════════════╝",
    );
    console.log(`🆔 Dart ID: ${this.darId}`);

    this.darService
      .getMembers(this.darId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (members) => {
          console.log(`✅ Loaded ${members.length} members:`, members);
          if (this.darDetails) {
            this.darDetails.members = members.map((m: any) => ({
              id: m.id || "",
              name: m.user?.userName || "Unknown",
              email: m.user?.email || "",
              avatar: this.getDefaultAvatar(),
              role: m.permission === "ORGANIZER" ? "organizer" : "member",
              status: m.status as MemberStatus, // PENDING, ACTIVE, or LEAVED
              turnDate: "TBD", // Will be updated from rounds
            }));
            console.log(
              `📊 Mapped ${this.darDetails.members.length} members for display`,
            );
            console.log(
              `📋 Member IDs:`,
              this.darDetails.members.map((m) => ({
                name: m.name,
                id: m.id,
              })),
            );
            
            // Update turn dates from rounds if rounds are already loaded
            if (this.rounds.length > 0) {
              this.updateMemberTurnDates();
            }
          }
        },
        error: (err) => {
          console.error("❌ Error loading members:");
          console.error("  - Dart ID:", this.darId);
          console.error("  - Error:", err);
          // Keep empty members list
        },
      });
  }

  /**
   * Load rounds for the dart
   */
  private loadRounds(): void {
    if (!this.darId) return;

    this.isLoadingRounds = true;
    console.log("🔄 Loading rounds for dart:", this.darId);

    // Load all rounds
    this.roundService
      .getRoundsByDartId(this.darId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (rounds) => {
          console.log(`✅ Loaded ${rounds.length} rounds:`, rounds);
          console.log("📋 Round details:", rounds.map(r => ({
            id: r.id,
            number: r.number,
            recipientMemberId: r.recipientMemberId,
            recipientMemberName: r.recipientMemberName,
            date: r.date,
            status: r.status
          })));
          
          this.rounds = rounds.sort((a, b) => a.number - b.number);
          this.isLoadingRounds = false;

          // Set current round = first INPAYED round (next to receive money)
          this.currentRound = this.rounds.find((r) => r.status === 'INPAYED') ?? null;

          // Update member turn dates based on rounds (if members are already loaded)
          if (this.darDetails && this.darDetails.members.length > 0) {
            this.updateMemberTurnDates();
          }

          // Optionally sync from backend (don't replace if we already have one)
          if (!this.currentRound && this.rounds.length > 0) {
            this.loadCurrentRound();
          }
        },
        error: (err) => {
          console.error("❌ Error loading rounds:", err);
          console.error("  - Dart ID:", this.darId);
          console.error("  - Status Code:", err.status);
          console.error("  - Error Message:", err.error?.message || err.message);
          console.error("  - Full Error:", err);
          this.isLoadingRounds = false;
          // Show error in console but don't break the UI
          // Rounds might not exist yet if dart just started
        },
      });
  }

  /**
   * Load current round from backend (optional fallback when no INPAYED in list)
   */
  private loadCurrentRound(): void {
    if (!this.darId) return;

    this.roundService
      .getCurrentRound(this.darId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (round) => {
          console.log("✅ Current round from API:", round);
          this.currentRound = round;
        },
        error: () => {
          // Keep currentRound from rounds list; only clear if we never had one
          if (!this.currentRound) {
            console.log("ℹ️ No current round from API (all rounds may be paid)");
          }
        },
      });
  }

  /**
   * Update member turn dates based on rounds
   */
  private updateMemberTurnDates(): void {
    if (!this.darDetails || !this.rounds.length) {
      console.log("⚠️ Cannot update turn dates - missing data:", {
        hasDarDetails: !!this.darDetails,
        roundsCount: this.rounds.length
      });
      return;
    }

    console.log("🔄 Updating member turn dates from rounds...");
    console.log("📋 Rounds:", this.rounds.map(r => ({
      number: r.number,
      recipientMemberId: r.recipientMemberId,
      date: r.date
    })));
    console.log("👥 Members:", this.darDetails.members.map(m => ({
      name: m.name,
      id: m.id
    })));

    // Map rounds to members by recipient
    let updatedCount = 0;
    this.darDetails.members.forEach((member) => {
      const round = this.rounds.find(
        (r) => r.recipientMemberId === member.id
      );
      if (round) {
        member.turnDate = formatRoundDate(round.date);
        updatedCount++;
        console.log(`✅ Updated ${member.name}: ${member.turnDate} (Round ${round.number})`);
      } else {
        console.log(`⚠️ No round found for member ${member.name} (ID: ${member.id})`);
        // Keep TBD if no round found
        if (!member.turnDate || member.turnDate === "TBD") {
          member.turnDate = "TBD";
        }
      }
    });
    
    console.log(`✅ Updated ${updatedCount} out of ${this.darDetails.members.length} member turn dates`);
  }

  /**
   * Maps API response to component display format
   */
  private mapApiDataToComponent(apiData: any): DarDetails {
    console.log(
      "╔═══════════════════════════════════════════════════════════╗",
    );
    console.log("║  🔄 MAPPING API DATA TO COMPONENT                        ║");
    console.log(
      "╚═══════════════════════════════════════════════════════════╝",
    );
    console.log("📥 INPUT from API:");
    console.log("  - apiData.id:", apiData.id);
    console.log("  - apiData.name:", apiData.name);
    console.log("  - apiData.organizerName:", apiData.organizerName);
    console.log("  - apiData.status:", apiData.status);
    console.log("  - apiData.memberCount:", apiData.memberCount);

    const mapped = {
      id: apiData.id,
      name: apiData.name,
      image: apiData.image || this.getDefaultDarImage(),
      status: apiData.status?.toLowerCase() || "pending",
      organizer: apiData.organizerName || "Unknown",
      startDate: apiData.startDate
        ? new Date(apiData.startDate).toLocaleDateString()
        : "Not started",
      currentCycle: apiData.currentCycle || 0,
      totalCycles: apiData.totalCycles || apiData.memberCount || 0,
      progress:
        apiData.totalCycles > 0
          ? Math.round((apiData.currentCycle / apiData.totalCycles) * 100)
          : 0,
      totalMembers: apiData.memberCount || 0,
      monthlyPot: apiData.totalMonthlyPool || 0,
      nextPayout: apiData.nextPayoutDate
        ? new Date(apiData.nextPayoutDate).toLocaleDateString()
        : "TBD",
      members: [], // Will be loaded separately
    };

    console.log("📤 OUTPUT to component:");
    console.log("  - mapped.id:", mapped.id);
    console.log("  - mapped.name:", mapped.name);
    console.log("  - mapped.organizer:", mapped.organizer);
    console.log("  - mapped.totalMembers:", mapped.totalMembers);
    console.log("  - mapped.status:", mapped.status);
    console.log("  - mapped.monthlyPot:", mapped.monthlyPot);

    return mapped;
  }

  private getDefaultDarImage(): string {
    return "https://lh3.googleusercontent.com/aida-public/AB6AXuD-pwl5GpoIiCkLChiBfM8jBx93qoPfudmizHw4SYrGPAqpLh5iKHnmvode9ncSTcX8DL1gns4F-ae0xNAH2xQWvXgtnTjKu5cagVr0TiEYflQKOxiwOrFxGbm1qTtC_VlRRNYQHS6LHhbRVzoWAn5wWzTm5bGQOLBVHVB9JrBaeYViaKMu-hXjEk-0ZtPSBjmCGFmqrQc9uLkPJD9riQH3WUuNmAKZ5Q8LbrwZ3xayNu7hbn0QqNuKxXqKRl_8x53-0trppiHouNs";
  }

  private getDefaultAvatar(): string {
    return "https://lh3.googleusercontent.com/aida-public/AB6AXuBslXgr8opXf_uENdwJxzyKr9GRCw4G8mfyE8iZQQOngvypPi6214ULvdRZBuUvsIy0WRk3RdepAhh9-RL3tcVRMVvxfL9JUcmM12GxvjNk0oLlZRbTs0oCabm2iHzsq1TLpJG9oDmzaXryRQMyAngNT1Tdf14qsZ59ySzC37XJ8X06OXxX09aAZwtdAq4eTBxG3InSq6T5Drk7Fqk3HIqapsMv96l1zWzUSkStz23i_xUVPiqeKp3MKwHTg3E3eEbdsnnglO56Axs";
  }

  /**
   * Get filtered members based on search query
   */
  get filteredMembers(): Member[] {
    if (!this.darDetails || !this.searchQuery) {
      return this.darDetails?.members || [];
    }

    const query = this.searchQuery.toLowerCase();
    return this.darDetails.members.filter(
      (member) =>
        member.name.toLowerCase().includes(query) ||
        member.email.toLowerCase().includes(query),
    );
  }

  /**
   * Switch active tab
   */
  setTab(tab: "members" | "tours" | "messages" | "settings"): void {
    this.activeTab = tab;
  }

  /**
   * Handle search input
   */
  onSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchQuery = input.value;
  }

  /**
   * Open invite member modal
   */
  inviteMember(): void {
    this.showInviteModal = true;
    this.inviteSearchQuery = "";
    this.searchResults = [];
  }

  /**
   * Close invite modal
   */
  closeInviteModal(): void {
    this.showInviteModal = false;
    this.inviteSearchQuery = "";
    this.searchResults = [];
    this.isSearching = false;
    this.invitingUserId = null;
  }

  /**
   * Search for users to invite (real-time API call)
   */
  searchUsers(): void {
    const query = this.inviteSearchQuery.trim();

    if (!query) {
      this.searchResults = [];
      return;
    }

    if (query.length < 2) {
      this.searchResults = [];
      return;
    }

    console.log("🔍 Searching users with query:", query);
    this.isSearching = true;

    // Get current member IDs to exclude them from results
    const memberIds = this.darDetails?.members.map((m) => m.id) || [];

    this.darService
      .searchUsers(query)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isSearching = false)),
      )
      .subscribe({
        next: (users) => {
          console.log("✅ Found users:", users);
          // Filter out users who are already members
          this.searchResults = users
            .filter((u) => !memberIds.includes(u.id))
            .map((u) => ({
              id: u.id,
              name: u.userName,
              email: u.email,
              avatar: u.avatar || this.getDefaultAvatar(),
            }));
          console.log(
            "📋 Filtered results (excluding members):",
            this.searchResults,
          );
        },
        error: (err) => {
          console.error("❌ Error searching users:", err);
          this.searchResults = [];
        },
      });
  }

  /**
   * Invite a user to the Dâr (real API call)
   */
  inviteUser(userId: string): void {
    if (!this.darId) return;

    const user = this.searchResults.find((u) => u.id === userId);
    if (!user) return;

    console.log("📧 Inviting user:", user.name, "to dart:", this.darId);
    this.invitingUserId = userId;

    this.darService
      .inviteMember(this.darId, userId)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.invitingUserId = null)),
      )
      .subscribe({
        next: () => {
          console.log("✅ User invited successfully");
          // Remove from search results
          this.searchResults = this.searchResults.filter(
            (u) => u.id !== userId,
          );
          // Show success message
          alert(`${user.name} has been invited to this Dâr!`);
          // Reload members list
          this.loadMembers();
        },
        error: (err) => {
          console.error("❌ Error inviting member:", err);
          const errorMessage =
            err.error?.message ||
            "Failed to invite member. They may already be a member.";
          alert(errorMessage);
        },
      });
  }

  /**
   * Start the Dâr (organizer only)
   */
  startDart(): void {
    if (!this.darId) return;

    if (
      !confirm(
        "Are you sure you want to start this Dâr? This will activate the dart and begin the contribution cycles.",
      )
    ) {
      return;
    }

    console.log("🚀 Starting Dart:", this.darId);
    this.isLoading = true;

    this.darService
      .startDar(this.darId)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isLoading = false)),
      )
      .subscribe({
        next: (response) => {
          console.log("✅ Dart started successfully:", response);
          // Reload the dart details to show updated status
          this.loadDarDetails();
          // Rounds will be loaded automatically when dart status is ACTIVE
          alert("Dâr started successfully! Rounds have been created.");
        },
        error: (err) => {
          console.error("❌ Error starting dart:", err);
          const errorMessage =
            err.error?.message ||
            "Failed to start Dâr. Please ensure you have the minimum number of members.";
          alert(errorMessage);
        },
      });
  }

  /**
   * Share Dâr link
   */
  shareLink(): void {
    console.log("Share link clicked");
    // TODO: Implement share functionality
    // Example: Copy link to clipboard, show share modal, etc.
    alert("Share link functionality coming soon!");
  }

  /**
   * Send reminder to a member about pending payment
   */
  remindMember(memberId: string): void {
    console.log("Remind member:", memberId);
    // TODO: Implement reminder functionality
    // Example API call:
    // this.darService.sendReminder(this.darId, memberId).subscribe(...)
    alert(`Reminder sent to member ${memberId}`);
  }

  /**
   * Open member options menu
   */
  openMemberOptions(memberId: string): void {
    console.log("Open options for member:", memberId);
    // TODO: Implement options menu
    // Example: Show dropdown with actions like remove, make admin, etc.
  }

  /**
   * Format round date for display
   */
  formatRoundDate(dateString: string): string {
    return formatRoundDate(dateString);
  }

  /**
   * Get relative time for round date
   */
  getRelativeTime(dateString: string): string {
    return getRelativeTime(dateString);
  }

  /**
   * Get round status label
   */
  getRoundStatusLabel(status: RoundStatus | string): string {
    return getRoundStatusLabel(status as RoundStatus);
  }

  /**
   * Get round status color classes
   */
  getRoundStatusColor(status: RoundStatus | string): string {
    return getRoundStatusColor(status as RoundStatus);
  }

  /**
   * Rounds for display: current (next) round first, then the rest by number
   */
  get roundsForDisplay(): Round[] {
    if (!this.rounds.length) return [];
    if (!this.currentRound) return [...this.rounds];
    const current = this.rounds.find((r) => r.id === this.currentRound?.id);
    if (!current) return [...this.rounds];
    const rest = this.rounds.filter((r) => r.id !== current.id);
    return [current, ...rest];
  }

  /**
   * Contribution per person for a round (each member pays this amount)
   * Total pot = amount; each pays amount / totalMembers
   */
  getContributionPerPerson(round: Round): number {
    if (!this.darDetails?.totalMembers || this.darDetails.totalMembers <= 0) {
      return round.amount ?? 0;
    }
    return (round.amount ?? 0) / this.darDetails.totalMembers;
  }

  /**
   * Get CSS classes for member status badges
   */
  getStatusClass(status: MemberStatus): string {
    switch (status) {
      case MemberStatus.ACTIVE:
        return "bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-400 ring-green-600/20";
      case MemberStatus.PENDING:
        return "bg-yellow-50 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400 ring-yellow-600/20";
      case MemberStatus.LEAVED:
        return "bg-gray-50 dark:bg-gray-800/50 text-gray-600 dark:text-gray-400 ring-gray-600/20";
      default:
        return "bg-gray-50 dark:bg-gray-800/50 text-gray-600 dark:text-gray-400 ring-gray-600/20";
    }
  }

  /**
   * Get Material icon name for payment status
   */
  getStatusIcon(status: MemberStatus): string {
    switch (status) {
      case MemberStatus.ACTIVE:
        return "check_circle";
      case MemberStatus.PENDING:
        return "schedule";
      case MemberStatus.LEAVED:
        return "logout";
      default:
        return "help";
    }
  }

  /**
   * Get display text for payment status
   */
  getStatusText(status: MemberStatus): string {
    switch (status) {
      case MemberStatus.ACTIVE:
        return "Active";
      case MemberStatus.PENDING:
        return "Pending Invitation";
      case MemberStatus.LEAVED:
        return "Left";
      default:
        return "Unknown";
    }
  }
}

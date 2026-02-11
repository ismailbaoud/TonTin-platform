import { Component, OnInit, OnDestroy } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ActivatedRoute, Router, RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { Subject, takeUntil, finalize } from "rxjs";

// Interfaces for component data
interface Member {
  id: number;
  name: string;
  email: string;
  avatar: string;
  role: "organizer" | "member";
  turnDate: string;
  paymentStatus: "paid" | "pending" | "overdue" | "future";
}

interface DarDetails {
  id: number;
  name: string;
  image: string;
  status: "active" | "completed" | "pending";
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
  darId: string | null = null;
  isLoading = false;
  error: string | null = null;

  // Invite modal state
  showInviteModal = false;
  inviteSearchQuery = "";
  searchResults: Array<{
    id: number;
    name: string;
    email: string;
    avatar: string;
  }> = [];
  isSearching = false;
  invitingUserId: number | null = null;

  // Data
  darDetails: DarDetails | null = null;

  // Mock users for search (easy to replace with API later)
  private mockUsers = [
    {
      id: 101,
      name: "John Smith",
      email: "john.smith@example.com",
      avatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBuVdVI9W4xZa3wFEpKvCeSRt4x54u_T7PV9TQi_enyBYh-wEK5tQGVJFTKHAtbadhsH-yk6CYCWAyXUAa2VZYGCpm1_jNVqLSarYBKHTmVDy9m0nwRCYC88Ck9iQBY2le0hsDmHtGldoA4IrCNsR6yRFwKacc-6lIWQSB6GQZ8npZG1ZiPMhTk7miDZi66relrHecNCSSiECSD1gSSThCHQVd17M_g1gRGhMenIfY4MUZQnlGmZ9WO5cyoIR-rPswo9XJYd5CpM_k",
    },
    {
      id: 102,
      name: "Alice Johnson",
      email: "alice.j@example.com",
      avatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuCXFHgZ5GS2ufqUtsOkxSaXbP7nVlMYQ4qNRH_BIOwXDBfAglDPWd6X1JpTyPaVBMPw45GVGhRoPo4RrzV6L9xuSppSyULK4qU2uS82gjgCSXIgV6aPvx4AB1rThReMf_zKfDpMAwexRU6D_wFZnvRKGyFdvB2TcF9hrjioOVwFArzexfNL3yEPLBAuBPkjwhxZYNko4YvnPwRANNcGd2uIuxttmBAnVzJzWI01dDWHI-xHsm46BWjQiKqZsP3vuvFijR2xV6YLzaw",
    },
    {
      id: 103,
      name: "Bob Williams",
      email: "bob.w@example.com",
      avatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBbmW0NnKreIcj4qRJ7pm-qfDj5L_FUTEpW1HIrKgDTL9gNjrrhw8Ep0hHBlBw4mcLD9KfDmsoiudGzmzfiZ1NWY7YczAtKXHEAnR7sM9uvIXl3eO_UT2vLQGnqCrml0wzMGjaJu2H-ax40LTVbKBqlHe9ElGbmkk-Gxa-1PkmwT9Fmf3B3GI4o6Wsk-gqSsB5uMrsZa-sXhkXz58j5egSgNajPmBPgv0tasDjUaZ5tDoi_Ew6wCafLLURiwBuLFNEfb_877JnlKH0",
    },
    {
      id: 104,
      name: "Carol Martinez",
      email: "carol.m@example.com",
      avatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuAK5lO6wcPdAIyCU22g_Fh88Y1wctZtZYFrH_-ycXeAQ0JAHmVNk1F4jTlS8KxC4ahBJzVznI2rRzJC8s-sna6jDGDplS4vnT__kTCGPcIFGaSg5o_NwgHgLSS5oyKCatxhinR7yPT-8yg9KGXrBw5y9-ctXM95v6Cm0NsfkvGTH-72sorrMqzpO-cXOs5fhYqZkFFccae3ibshhqr4nzpRJzvldVs4xzs0-qS70tq-Wdeu2MizopS_8VkPX9WOeMMSY_7lSUA85A0",
    },
    {
      id: 105,
      name: "David Brown",
      email: "david.brown@example.com",
      avatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBuVdVI9W4xZa3wFEpKvCeSRt4x54u_T7PV9TQi_enyBYh-wEK5tQGVJFTKHAtbadhsH-yk6CYCWAyXUAa2VZYGCpm1_jNVqLSarYBKHTmVDy9m0nwRCYC88Ck9iQBY2le0hsDmHtGldoA4IrCNsR6yRFwKacc-6lIWQSB6GQZ8npZG1ZiPMhTk7miDZi66relrHecNCSSiECSD1gSSThCHQVd17M_g1gRGhMenIfY4MUZQnlGmZ9WO5cyoIR-rPswo9XJYd5CpM_k",
    },
  ];

  // Static mock data (easy to replace with API later)
  private mockData: DarDetails = {
    id: 1,
    name: "Family Savings 2024",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuCBCoz3NomWh-7q6mKCsR4e_tP3VZTKWofatO1Lqo92rcIhtvZ70vQpFH7hOZJVb_uZG3D_yVvrDVQgDxo8HdcZOB1DJMvO2moD4yz08nJuf2XJRUAKYV1JfONNyiNn_Btnrg23jeONbLh36MRHEDl99zZtJ_OMbrC617RD68Yk_GEep6QyRy94DgDSlnmLcq2HnXBvgR7PJw-ymEQhK2H-2R4EECMBvaz6SkhSUcFGlpMsvZ-Dg3zjR4t6xPBZ4J4zEtcEW00ZEFE",
    status: "active",
    organizer: "Jane Doe",
    startDate: "Oct 1, 2023",
    currentCycle: 2,
    totalCycles: 12,
    progress: 16.66,
    totalMembers: 12,
    monthlyPot: 1200,
    nextPayout: "Nov 1",
    members: [
      {
        id: 1,
        name: "Jane Doe",
        email: "jane@example.com",
        avatar:
          "https://lh3.googleusercontent.com/aida-public/AB6AXuBuVdVI9W4xZa3wFEpKvCeSRt4x54u_T7PV9TQi_enyBYh-wEK5tQGVJFTKHAtbadhsH-yk6CYCWAyXUAa2VZYGCpm1_jNVqLSarYBKHTmVDy9m0nwRCYC88Ck9iQBY2le0hsDmHtGldoA4IrCNsR6yRFwKacc-6lIWQSB6GQZ8npZG1ZiPMhTk7miDZi66relrHecNCSSiECSD1gSSThCHQVd17M_g1gRGhMenIfY4MUZQnlGmZ9WO5cyoIR-rPswo9XJYd5CpM_k",
        role: "organizer",
        turnDate: "Dec 1, 2023",
        paymentStatus: "paid",
      },
      {
        id: 2,
        name: "Michael Foster",
        email: "michael@example.com",
        avatar:
          "https://lh3.googleusercontent.com/aida-public/AB6AXuCXFHgZ5GS2ufqUtsOkxSaXbP7nVlMYQ4qNRH_BIOwXDBfAglDPWd6X1JpTyPaVBMPw45GVGhRoPo4RrzV6L9xuSppSyULK4qU2uS82gjgCSXIgV6aPvx4AB1rThReMf_zKfDpMAwexRU6D_wFZnvRKGyFdvB2TcF9hrjioOVwFArzexfNL3yEPLBAuBPkjwhxZYNko4YvnPwRANNcGd2uIuxttmBAnVzJzWI01dDWHI-xHsm46BWjQiKqZsP3vuvFijR2xV6YLzaw",
        role: "member",
        turnDate: "Nov 1, 2023",
        paymentStatus: "paid",
      },
      {
        id: 3,
        name: "Dries Vincent",
        email: "dries@example.com",
        avatar:
          "https://lh3.googleusercontent.com/aida-public/AB6AXuBbmW0NnKreIcj4qRJ7pm-qfDj5L_FUTEpW1HIrKgDTL9gNjrrhw8Ep0hHBlBw4mcLD9KfDmsoiudGzmzfiZ1NWY7YczAtKXHEAnR7sM9uvIXl3eO_UT2vLQGnqCrml0wzMGjaJu2H-ax40LTVbKBqlHe9ElGbmkk-Gxa-1PkmwT9Fmf3B3GI4o6Wsk-gqSsB5uMrsZa-sXhkXz58j5egSgNajPmBPgv0tasDjUaZ5tDoi_Ew6wCafLLURiwBuLFNEfb_877JnlKH0",
        role: "member",
        turnDate: "Jan 1, 2024",
        paymentStatus: "pending",
      },
      {
        id: 4,
        name: "Lindsay Walton",
        email: "lindsay@example.com",
        avatar:
          "https://lh3.googleusercontent.com/aida-public/AB6AXuAK5lO6wcPdAIyCU22g_Fh88Y1wctZtZYFrH_-ycXeAQ0JAHmVNk1F4jTlS8KxC4ahBJzVznI2rRzJC8s-sna6jDGDplS4vnT__kTCGPcIFGaSg5o_NwgHgLSS5oyKCatxhinR7yPT-8yg9KGXrBw5y9-ctXM95v6Cm0NsfkvGTH-72sorrMqzpO-cXOs5fhYqZkFFccae3ibshhqr4nzpRJzvldVs4xzs0-qS70tq-Wdeu2MizopS_8VkPX9WOeMMSY_7lSUA85A0",
        role: "member",
        turnDate: "Feb 1, 2024",
        paymentStatus: "future",
      },
      {
        id: 5,
        name: "Sarah Connor",
        email: "sarah@example.com",
        avatar:
          "https://lh3.googleusercontent.com/aida-public/AB6AXuBuVdVI9W4xZa3wFEpKvCeSRt4x54u_T7PV9TQi_enyBYh-wEK5tQGVJFTKHAtbadhsH-yk6CYCWAyXUAa2VZYGCpm1_jNVqLSarYBKHTmVDy9m0nwRCYC88Ck9iQBY2le0hsDmHtGldoA4IrCNsR6yRFwKacc-6lIWQSB6GQZ8npZG1ZiPMhTk7miDZi66relrHecNCSSiECSD1gSSThCHQVd17M_g1gRGhMenIfY4MUZQnlGmZ9WO5cyoIR-rPswo9XJYd5CpM_k",
        role: "member",
        turnDate: "Mar 1, 2024",
        paymentStatus: "future",
      },
      {
        id: 6,
        name: "Tom Hardy",
        email: "tom@example.com",
        avatar:
          "https://lh3.googleusercontent.com/aida-public/AB6AXuCXFHgZ5GS2ufqUtsOkxSaXbP7nVlMYQ4qNRH_BIOwXDBfAglDPWd6X1JpTyPaVBMPw45GVGhRoPo4RrzV6L9xuSppSyULK4qU2uS82gjgCSXIgV6aPvx4AB1rThReMf_zKfDpMAwexRU6D_wFZnvRKGyFdvB2TcF9hrjioOVwFArzexfNL3yEPLBAuBPkjwhxZYNko4YvnPwRANNcGd2uIuxttmBAnVzJzWI01dDWHI-xHsm46BWjQiKqZsP3vuvFijR2xV6YLzaw",
        role: "member",
        turnDate: "Apr 1, 2024",
        paymentStatus: "future",
      },
      {
        id: 7,
        name: "Emma Stone",
        email: "emma@example.com",
        avatar:
          "https://lh3.googleusercontent.com/aida-public/AB6AXuBbmW0NnKreIcj4qRJ7pm-qfDj5L_FUTEpW1HIrKgDTL9gNjrrhw8Ep0hHBlBw4mcLD9KfDmsoiudGzmzfiZ1NWY7YczAtKXHEAnR7sM9uvIXl3eO_UT2vLQGnqCrml0wzMGjaJu2H-ax40LTVbKBqlHe9ElGbmkk-Gxa-1PkmwT9Fmf3B3GI4o6Wsk-gqSsB5uMrsZa-sXhkXz58j5egSgNajPmBPgv0tasDjUaZ5tDoi_Ew6wCafLLURiwBuLFNEfb_877JnlKH0",
        role: "member",
        turnDate: "May 1, 2024",
        paymentStatus: "future",
      },
      {
        id: 8,
        name: "Chris Evans",
        email: "chris@example.com",
        avatar:
          "https://lh3.googleusercontent.com/aida-public/AB6AXuAK5lO6wcPdAIyCU22g_Fh88Y1wctZtZYFrH_-ycXeAQ0JAHmVNk1F4jTlS8KxC4ahBJzVznI2rRzJC8s-sna6jDGDplS4vnT__kTCGPcIFGaSg5o_NwgHgLSS5oyKCatxhinR7yPT-8yg9KGXrBw5y9-ctXM95v6Cm0NsfkvGTH-72sorrMqzpO-cXOs5fhYqZkFFccae3ibshhqr4nzpRJzvldVs4xzs0-qS70tq-Wdeu2MizopS_8VkPX9WOeMMSY_7lSUA85A0",
        role: "member",
        turnDate: "Jun 1, 2024",
        paymentStatus: "future",
      },
      {
        id: 9,
        name: "Natalie Portman",
        email: "natalie@example.com",
        avatar:
          "https://lh3.googleusercontent.com/aida-public/AB6AXuBuVdVI9W4xZa3wFEpKvCeSRt4x54u_T7PV9TQi_enyBYh-wEK5tQGVJFTKHAtbadhsH-yk6CYCWAyXUAa2VZYGCpm1_jNVqLSarYBKHTmVDy9m0nwRCYC88Ck9iQBY2le0hsDmHtGldoA4IrCNsR6yRFwKacc-6lIWQSB6GQZ8npZG1ZiPMhTk7miDZi66relrHecNCSSiECSD1gSSThCHQVd17M_g1gRGhMenIfY4MUZQnlGmZ9WO5cyoIR-rPswo9XJYd5CpM_k",
        role: "member",
        turnDate: "Jul 1, 2024",
        paymentStatus: "future",
      },
      {
        id: 10,
        name: "Robert Downey",
        email: "robert@example.com",
        avatar:
          "https://lh3.googleusercontent.com/aida-public/AB6AXuCXFHgZ5GS2ufqUtsOkxSaXbP7nVlMYQ4qNRH_BIOwXDBfAglDPWd6X1JpTyPaVBMPw45GVGhRoPo4RrzV6L9xuSppSyULK4qU2uS82gjgCSXIgV6aPvx4AB1rThReMf_zKfDpMAwexRU6D_wFZnvRKGyFdvB2TcF9hrjioOVwFArzexfNL3yEPLBAuBPkjwhxZYNko4YvnPwRANNcGd2uIuxttmBAnVzJzWI01dDWHI-xHsm46BWjQiKqZsP3vuvFijR2xV6YLzaw",
        role: "member",
        turnDate: "Aug 1, 2024",
        paymentStatus: "future",
      },
      {
        id: 11,
        name: "Scarlett Johansson",
        email: "scarlett@example.com",
        avatar:
          "https://lh3.googleusercontent.com/aida-public/AB6AXuBbmW0NnKreIcj4qRJ7pm-qfDj5L_FUTEpW1HIrKgDTL9gNjrrhw8Ep0hHBlBw4mcLD9KfDmsoiudGzmzfiZ1NWY7YczAtKXHEAnR7sM9uvIXl3eO_UT2vLQGnqCrml0wzMGjaJu2H-ax40LTVbKBqlHe9ElGbmkk-Gxa-1PkmwT9Fmf3B3GI4o6Wsk-gqSsB5uMrsZa-sXhkXz58j5egSgNajPmBPgv0tasDjUaZ5tDoi_Ew6wCafLLURiwBuLFNEfb_877JnlKH0",
        role: "member",
        turnDate: "Sep 1, 2024",
        paymentStatus: "future",
      },
      {
        id: 12,
        name: "Mark Ruffalo",
        email: "mark@example.com",
        avatar:
          "https://lh3.googleusercontent.com/aida-public/AB6AXuAK5lO6wcPdAIyCU22g_Fh88Y1wctZtZYFrH_-ycXeAQ0JAHmVNk1F4jTlS8KxC4ahBJzVznI2rRzJC8s-sna6jDGDplS4vnT__kTCGPcIFGaSg5o_NwgHgLSS5oyKCatxhinR7yPT-8yg9KGXrBw5y9-ctXM95v6Cm0NsfkvGTH-72sorrMqzpO-cXOs5fhYqZkFFccae3ibshhqr4nzpRJzvldVs4xzs0-qS70tq-Wdeu2MizopS_8VkPX9WOeMMSY_7lSUA85A0",
        role: "member",
        turnDate: "Oct 1, 2024",
        paymentStatus: "future",
      },
    ],
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.darId = this.route.snapshot.paramMap.get("id");
    this.loadDarDetails();
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
    // Simulate loading
    this.isLoading = true;
    this.error = null;

    // Simulate network delay
    setTimeout(() => {
      try {
        // TODO: Replace with actual API call
        // Example:
        // this.darService.getDarDetails(+this.darId!)
        //   .pipe(takeUntil(this.destroy$))
        //   .subscribe({
        //     next: (data) => { this.darDetails = this.mapApiData(data); },
        //     error: (err) => { this.error = err.message; }
        //   });

        this.darDetails = this.mockData;
        this.isLoading = false;
      } catch (err) {
        this.error = "Failed to load Dâr details";
        this.isLoading = false;
      }
    }, 500);
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
   * Search for users to invite
   */
  searchUsers(): void {
    if (!this.inviteSearchQuery.trim()) {
      this.searchResults = [];
      return;
    }

    this.isSearching = true;

    // Simulate API call with delay
    setTimeout(() => {
      const query = this.inviteSearchQuery.toLowerCase().trim();

      // Filter mock users (exclude already members)
      const memberIds = this.darDetails?.members.map((m) => m.id) || [];
      this.searchResults = this.mockUsers.filter(
        (user) =>
          !memberIds.includes(user.id) &&
          (user.name.toLowerCase().includes(query) ||
            user.email.toLowerCase().includes(query)),
      );

      this.isSearching = false;

      // TODO: Replace with actual API call
      // this.darService.searchUsers(this.inviteSearchQuery)
      //   .pipe(takeUntil(this.destroy$))
      //   .subscribe({
      //     next: (users) => {
      //       this.searchResults = users.filter(u => !memberIds.includes(u.id));
      //       this.isSearching = false;
      //     },
      //     error: (err) => {
      //       console.error("Error searching users:", err);
      //       this.isSearching = false;
      //     }
      //   });
    }, 300);
  }

  /**
   * Invite a user to the Dâr
   */
  inviteUser(userId: number): void {
    if (!this.darId) return;

    this.invitingUserId = userId;

    // Simulate API call
    setTimeout(() => {
      // TODO: Replace with actual API call
      // this.darService.inviteMember({ darId: +this.darId!, userId })
      //   .pipe(takeUntil(this.destroy$))
      //   .subscribe({
      //     next: () => {
      //       // Remove from search results
      //       this.searchResults = this.searchResults.filter(u => u.id !== userId);
      //       this.invitingUserId = null;
      //       // Show success message
      //       // Optionally refresh the member list
      //     },
      //     error: (err) => {
      //       console.error("Error inviting member:", err);
      //       this.invitingUserId = null;
      //     }
      //   });

      // Mock: Remove from search results
      this.searchResults = this.searchResults.filter((u) => u.id !== userId);
      this.invitingUserId = null;

      // Show success feedback
      const user = this.mockUsers.find((u) => u.id === userId);
      if (user) {
        alert(`Invitation sent to ${user.name}!`);
      }
    }, 500);
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
  remindMember(memberId: number): void {
    console.log("Remind member:", memberId);
    // TODO: Implement reminder functionality
    // Example API call:
    // this.darService.sendReminder(this.darId, memberId).subscribe(...)
    alert(`Reminder sent to member ${memberId}`);
  }

  /**
   * Open member options menu
   */
  openMemberOptions(memberId: number): void {
    console.log("Open options for member:", memberId);
    // TODO: Implement options menu
    // Example: Show dropdown with actions like remove, make admin, etc.
  }

  /**
   * Get CSS classes for payment status badge
   */
  getStatusClass(status: string): string {
    const statusLower = status.toLowerCase();
    switch (statusLower) {
      case "paid":
        return "bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-400 ring-green-600/20";
      case "pending":
        return "bg-yellow-50 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-400 ring-yellow-600/20";
      case "overdue":
        return "bg-red-50 dark:bg-red-900/30 text-red-800 dark:text-red-400 ring-red-600/20";
      case "future":
        return "bg-gray-50 dark:bg-gray-700/30 text-gray-600 dark:text-gray-400 ring-gray-500/10";
      default:
        return "bg-gray-50 dark:bg-gray-700/30 text-gray-600 dark:text-gray-400 ring-gray-500/10";
    }
  }

  /**
   * Get Material icon name for payment status
   */
  getStatusIcon(status: string): string {
    const statusLower = status.toLowerCase();
    switch (statusLower) {
      case "paid":
        return "check_circle";
      case "pending":
        return "schedule";
      case "overdue":
        return "error";
      case "future":
        return "remove";
      default:
        return "help";
    }
  }

  /**
   * Get display text for payment status
   */
  getStatusText(status: string): string {
    const statusLower = status.toLowerCase();
    switch (statusLower) {
      case "paid":
        return "Paid";
      case "pending":
        return "Pending";
      case "overdue":
        return "Overdue";
      case "future":
        return "Future";
      default:
        return "Unknown";
    }
  }
}
